package dev.inmo.plagubot

import dev.inmo.kslog.common.*
import dev.inmo.micro_utils.common.Warning
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.fsm.common.StatesManager
import dev.inmo.micro_utils.fsm.common.managers.*
import dev.inmo.micro_utils.koin.getAllDistinct
import dev.inmo.plagubot.config.*
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.bot.ktor.KtorRequestsExecutorBuilder
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.webhook.deleteWebhook
import dev.inmo.tgbotapi.extensions.behaviour_builder.*
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.startGettingOfUpdatesByLongPolling
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module

val Scope.plagubot: PlaguBot
    get() = get()

val Koin.plagubot: PlaguBot
    get() = get()

@OptIn(Warning::class)
@Serializable
data class PlaguBot(
    private val json: JsonObject,
    private val config: Config
) : Plugin {
    override fun KtorRequestsExecutorBuilder.setupBotClient(scope: Scope, params: JsonObject) {
        config.botPlugins.forEach {
            with(it) {
                setupBotClient(scope, params)
            }
        }
    }
    override fun KtorRequestsExecutorBuilder.setupBotClient() {
        config.botPlugins.forEach {
            with(it) {
                setupBotClient()
            }
        }
    }

    override fun Module.setupDI(config: JsonObject) {
        single { this@PlaguBot.config }
        single { this@PlaguBot.config.plugins }
        single { this@PlaguBot.config.databaseConfig }
        single { this@PlaguBot.config.databaseConfig.database }
        single { defaultJsonFormat }
        single { this@PlaguBot }
        single {
            val config = get<Config>()
            telegramBot(
                token = config.botToken,
                testServer = config.testServer,
                apiUrl = config.botApiServer
            ) {
                setupBotClient(this@single, json)
            }
        }
    }

    override fun Module.setupDI(database: Database, params: JsonObject) {
        setupDI(params)

        includes(
            config.botPlugins.mapNotNull {
                runCatching {
                    module {
                        with(it) {
                            setupDI(database, params)
                        }
                    }
                }.onFailure { e ->
                    logger.w(e) { "Unable to load DI part of $it" }
                }.getOrNull()
            }
        )
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)

        config.plugins.forEach { plugin ->
            runCatchingSafely {
                logger.i { "Starting of $plugin common logic" }
                with(plugin) {
                    startPlugin(koin)
                }
            }.onFailure { e ->
                logger.w(e) { "Unable to load common logic of $plugin" }
            }.onSuccess {
                logger.i { "Complete loading of $plugin common logic" }
            }
        }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        config.botPlugins.forEach { plugin ->
            runCatchingSafely {
                logger.i("Start loading of $plugin")
                with(plugin) {
                    setupBotPlugin(koin)
                }
            }.onFailure { e ->
                logger.w("Unable to load bot part of $plugin", e)
            }.onSuccess {
                logger.i("Complete loading of $plugin")
            }
        }
    }

    /**
     * This method will create an [Job] which will be the main [Job] of ran instance
     */
    suspend fun start(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    ): Job {
        logger.i("Start initialization")
        val koinApp = KoinApplication.init()
        koinApp.modules(
            module {
                setupDI(config.databaseConfig.database, json)
            }
        )
        logger.i("Modules loaded. Starting koin")
        GlobalContext.startKoin(koinApp)
        logger.i("Koin started. Starting plugins common logic")
        startPlugin(koinApp.koin)
        logger.i("Plugins common logic started. Starting setup of bot logic part")
        lateinit var behaviourContext: BehaviourContext
        val onStartContextsConflictResolver by lazy { koinApp.koin.getAllDistinct<OnStartContextsConflictResolver>() }
        val onUpdateContextsConflictResolver by lazy { koinApp.koin.getAllDistinct<OnUpdateContextsConflictResolver>() }
        val bot = koinApp.koin.get<TelegramBot>()
        bot.buildBehaviourWithFSM(
            scope = scope,
            defaultExceptionsHandler = {
                logger.e("Something went wrong", it)
            },
            statesManager = koinApp.koin.getOrNull<StatesManager<State>>() ?: DefaultStatesManager(
                koinApp.koin.getOrNull<DefaultStatesManagerRepo<State>>() ?: InMemoryDefaultStatesManagerRepo<State>(),
                onStartContextsConflictResolver = { old, new -> onStartContextsConflictResolver.firstNotNullOfOrNull { it(old, new) } ?: false },
                onUpdateContextsConflictResolver = { old, new, currentNew -> onUpdateContextsConflictResolver.firstNotNullOfOrNull { it(old, new, currentNew) } ?: false }
            ),
            onStateHandlingErrorHandler = koinApp.koin.getOrNull<StateHandlingErrorHandler<State>>() ?: { state, e ->
                logger.eS(e) { "Unable to handle state $state" }
                null
            }
        ) {
            logger.i("Start setup of bot part")
            behaviourContext = this
            setupBotPlugin(koinApp.koin)
            deleteWebhook()
        }.start()
        logger.i("Behaviour builder has been setup")
        return bot.startGettingOfUpdatesByLongPolling(scope = behaviourContext, updatesFilter = behaviourContext).also {
            logger.i("Long polling has been started")
        }
    }
}
