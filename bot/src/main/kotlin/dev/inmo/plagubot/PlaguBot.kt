package dev.inmo.plagubot

import dev.inmo.micro_utils.coroutines.runCatchingSafely
import dev.inmo.plagubot.config.*
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
import java.util.logging.Level
import java.util.logging.Logger

val Scope.plagubot: PlaguBot
    get() = get()

@Serializable
data class PlaguBot(
    private val json: JsonObject,
    private val config: Config
) : Plugin {
    @Transient
    private val logger = Logger.getLogger("PlaguBot")
    @Transient
    private val bot = telegramBot(config.botToken)

    override fun Module.setupDI(database: Database, params: JsonObject) {
        single { config }
        single { config.plugins }
        single { config.databaseConfig }
        single { config.databaseConfig.database }
        single { defaultJsonFormat }
        single { this@PlaguBot }

        includes(
            config.plugins.mapNotNull {
                runCatching {
                    module {
                        with(it) {
                            setupDI(database, params)
                        }
                    }
                }.onFailure { e ->
                    logger.log(Level.WARNING, "Unable to load DI part of $it", e)
                }.getOrNull()
            }
        )
    }

    override suspend fun BehaviourContext.setupBotPlugin(koin: Koin) {
        config.plugins.map {
            launch {
                runCatchingSafely {
                    logger.info("Start loading of $it")
                    with(it) {
                        setupBotPlugin(koin)
                    }
                }.onFailure { e ->
                    logger.log(Level.WARNING, "Unable to load bot part of $it", e)
                }.onSuccess {
                    logger.info("Complete loading of $it")
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
        logger.info("Start initialization")
        val koinApp = KoinApplication.init()
        koinApp.modules(
            module {
                setupDI(config.databaseConfig.database, json)
            }
        )
        logger.info("Modules loaded")
        GlobalContext.startKoin(koinApp)
        logger.info("Koin started")
        lateinit var behaviourContext: BehaviourContext
        bot.buildBehaviour(scope = scope) {
            logger.info("Start setup of bot part")
            behaviourContext = this
            setupBotPlugin(koinApp.koin)
            deleteWebhook()
        }
        logger.info("Behaviour builder has been setup")
        return bot.startGettingOfUpdatesByLongPolling(scope = behaviourContext, updatesFilter = behaviourContext).also {
            logger.info("Long polling has been started")
        }
    }
}
