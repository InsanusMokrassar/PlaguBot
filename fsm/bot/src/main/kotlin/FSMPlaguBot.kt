package dev.inmo.plagubot

import dev.inmo.kslog.common.*
import dev.inmo.micro_utils.common.Warning
import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.plagubot.config.Config
import dev.inmo.plagubot.config.defaultJsonFormat
import dev.inmo.plagubot.fsm.FSMPlugin
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.webhook.deleteWebhook
import dev.inmo.tgbotapi.extensions.behaviour_builder.*
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.startGettingOfUpdatesByLongPolling
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module

val Scope.fsmPlagubot: FSMPlaguBot
    get() = get()

val Koin.fsmPlagubot: FSMPlaguBot
    get() = get()

@OptIn(Warning::class)
@Serializable
data class FSMPlaguBot(
    private val json: JsonObject,
    private val config: Config
) : FSMPlugin {
    @Transient
    private val bot = telegramBot(config.botToken)

    override fun Module.setupDI(database: Database, params: JsonObject) {
        single { config }
        single { config.plugins }
        single { config.databaseConfig }
        single { config.databaseConfig.database }
        single { defaultJsonFormat }
        single { this@FSMPlaguBot }

        includes(
            config.plugins.mapNotNull {
                runCatching {
                    module {
                        with(it) {
                            setupDI(database, params)
                        }
                    }
                }.onFailure { e ->
                    logger.w("Unable to load DI part of $it", e)
                }.getOrNull()
            }
        )
    }

    override suspend fun BehaviourContextWithFSM<*>.setupBotPlugin(koin: Koin) {
        config.plugins.map { plugin ->
            launch {
                runCatchingSafely {
                    logger.i("Start loading of $plugin")
                    with(plugin) {
                        if (this is FSMPlugin) {
                            setupBotPlugin(koin) // use setupBotPlugin with BehaviourContextWithFSM as receiver
                        } else {
                            setupBotPlugin(koin) // use setupBotPlugin with BehaviourContext as receiver
                        }
                    }
                }.onFailure { e ->
                    logger.w("Unable to load bot part of $plugin", e)
                }.onSuccess {
                    logger.i("Complete loading of $plugin")
                }
            }
        }.joinAll()
    }

    /**
     * This method will create an [Job] which will be the main [Job] of ran instance
     */
    suspend fun start(
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): Job {
        logger.i("Start initialization")
        val koinApp = KoinApplication.init()
        koinApp.modules(
            module {
                setupDI(config.databaseConfig.database, json)
            }
        )
        logger.i("Modules loaded")
        GlobalContext.startKoin(koinApp)
        logger.i("Koin started")
        lateinit var behaviourContext: BehaviourContext
        bot.buildBehaviourWithFSM (scope = scope) {
            logger.i("Start setup of bot part")
            behaviourContext = this
            setupBotPlugin(koinApp.koin)
            deleteWebhook()
        }
        logger.i("Behaviour builder has been setup")
        return bot.startGettingOfUpdatesByLongPolling(scope = behaviourContext, updatesFilter = behaviourContext).also {
            logger.i("Long polling has been started")
        }
    }
}
