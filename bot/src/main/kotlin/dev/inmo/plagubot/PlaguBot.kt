package dev.inmo.plagubot

import dev.inmo.kslog.common.*
import dev.inmo.micro_utils.common.Warning
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.fsm.common.StatesManager
import dev.inmo.micro_utils.fsm.common.managers.*
import dev.inmo.micro_utils.koin.getAllDistinct
import dev.inmo.micro_utils.startup.launcher.StartLauncherPlugin
import dev.inmo.plagubot.config.*
import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.bot.ktor.KtorRequestsExecutorBuilder
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.webhook.deleteWebhook
import dev.inmo.tgbotapi.extensions.behaviour_builder.*
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.startGettingOfUpdatesByLongPolling
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import java.io.File

@OptIn(Warning::class)
@Serializable
object PlaguBot : Plugin {
    override fun KtorRequestsExecutorBuilder.setupBotClient(scope: Scope, params: JsonObject) {
        scope.plugins.filter { it !== this@PlaguBot }.forEach {
            with(it) {
                setupBotClient(scope, params)
            }
        }
    }

    override fun Module.setupDI(config: JsonObject) {
        single { get<Json>().decodeFromJsonElement(Config.serializer(), config) }
        single { config }
        single { get<Config>().databaseConfig }
        single { get<Config>().databaseConfig.database }
        single { this@PlaguBot }
        singlePlugins { get<dev.inmo.micro_utils.startup.launcher.Config>().plugins.filterIsInstance<Plugin>() }
        single {
            val config = get<Config>()
            telegramBot(
                token = config.botToken,
                testServer = config.testServer,
                apiUrl = config.botApiServer
            ) {
                setupBotClient(this@single, get<JsonObject>())
            }
        }
    }

    /**
     * Getting all [OnStartContextsConflictResolver], [OnUpdateContextsConflictResolver], [StatesManager] and [DefaultStatesManagerRepo]
     * and pass them into [buildBehaviourWithFSM] on top of [TelegramBot] took from [koin]. In time of
     * [buildBehaviourWithFSM] configuration will call [setupBotPlugin] and [deleteWebhook].
     *
     * After all preparation, the result of [buildBehaviourWithFSM] will be passed to [startGettingOfUpdatesByLongPolling]
     * as [CoroutineScope] and [UpdatesFilter].
     *
     * The [Job] took from [startGettingOfUpdatesByLongPolling] will be used to prevent app stopping by calling [Job.join]
     * on it
     */
    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        val scope = koin.get<CoroutineScope>()

        lateinit var behaviourContext: BehaviourContext
        val onStartContextsConflictResolver by lazy { koin.getAllDistinct<OnStartContextsConflictResolver>() }
        val onUpdateContextsConflictResolver by lazy { koin.getAllDistinct<OnUpdateContextsConflictResolver>() }
        val bot = koin.get<TelegramBot>()
        bot.buildBehaviourWithFSM(
            scope = scope,
            defaultExceptionsHandler = {
                logger.e("Something went wrong", it)
            },
            statesManager = koin.getOrNull<StatesManager<State>>() ?: DefaultStatesManager(
                koin.getOrNull<DefaultStatesManagerRepo<State>>() ?: InMemoryDefaultStatesManagerRepo<State>(),
                onStartContextsConflictResolver = { old, new -> onStartContextsConflictResolver.firstNotNullOfOrNull { it(old, new) } ?: false },
                onUpdateContextsConflictResolver = { old, new, currentNew -> onUpdateContextsConflictResolver.firstNotNullOfOrNull { it(old, new, currentNew) } ?: false }
            ),
            onStateHandlingErrorHandler = koin.getOrNull<StateHandlingErrorHandler<State>>() ?: { state, e ->
                logger.eS(e) { "Unable to handle state $state" }
                null
            }
        ) {
            logger.i("Start setup of bot part")
            behaviourContext = this
            setupBotPlugin(koin)
            deleteWebhook()
        }.start()
        logger.i("Behaviour builder has been setup")

        bot.startGettingOfUpdatesByLongPolling(scope = behaviourContext, updatesFilter = behaviourContext).also {
            logger.i("Long polling has been started")
        }.join()
    }

    /**
     * Initializing [Plugin]s from [koin] took by [plugins] extension. [PlaguBot] itself will be filtered out from
     * list of plugins to be inited
     */
    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        koin.plugins.filter { it !== this@PlaguBot }.forEach { plugin ->
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
     * Starting plugins system using [StartLauncherPlugin.start]. In time of parsing [initialJson] [PlaguBot] may
     * add itself in its `plugins` section in case of its absence there. So, by launching this [start] it is guaranteed
     * that [PlaguBot] will be in list of plugins to be loaded by [StartLauncherPlugin]
     */
    suspend fun start(initialJson: JsonObject): Job {
        val initialConfig = defaultJsonFormat.decodeFromJsonElement(dev.inmo.micro_utils.startup.launcher.Config.serializer(), initialJson)

        KSLog.i("Config has been read")

        // Adding of PlaguBot when it absent in config
        val (resultJson, resultConfig) = if (PlaguBot in initialConfig.plugins) {
            KSLog.i("Initial config contains PlaguBot, pass config as is to StartLauncherPlugin")
            initialJson to initialConfig
        } else {
            KSLog.i("Start fixing of PlaguBot absence. If PlaguBot has been skipped by some reason, use dev.inmo.micro_utils.startup.launcher.main as startup point or StartLauncherPlugin directly")
            val resultJson = JsonObject(
                initialJson + Pair("plugins", JsonArray(initialJson["plugins"]!!.jsonArray + JsonPrimitive(PlaguBot::class.qualifiedName!!)))
            )
            val resultConfig = defaultJsonFormat.decodeFromJsonElement(dev.inmo.micro_utils.startup.launcher.Config.serializer(), resultJson)
            resultJson to resultConfig
        }

        KSLog.i("Config initialization done. Passing config to StartLauncherPlugin")
        return StartLauncherPlugin.start(
            resultConfig,
            resultJson
        ).koin.get<CoroutineScope>().coroutineContext.job
    }

    /**
     * Accepts single argument in [args] which will be interpreted as [File] path with [StartLauncherPlugin]
     * configuration content. After reading of that file as [JsonObject] will pass it in [start] with [JsonObject] as
     * argument
     */
    suspend fun start(args: Array<String>): Job {
        KSLog.default = KSLog("PlaguBot")
        val (configPath) = args
        val file = File(configPath)
        KSLog.i("Start read config from ${file.absolutePath}")

        val initialJson = defaultJsonFormat.parseToJsonElement(file.readText()).jsonObject
        return start(initialJson)
    }
}
