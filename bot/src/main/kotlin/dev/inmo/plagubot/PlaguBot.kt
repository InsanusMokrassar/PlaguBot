package dev.inmo.plagubot

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

val Scope.plagubot: PlaguBot
    get() = get()

@Serializable
data class PlaguBot(
    private val json: JsonObject,
    private val config: Config
) : Plugin {
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
            config.plugins.map {
                module {
                    with(it) {
                        setupDI(database, params)
                    }
                }
            }
        )
    }

    override suspend fun BehaviourContext.setupBotPlugin(koin: Koin) {
        config.plugins.forEach {
            with(it) {
                setupBotPlugin(koin)
            }
        }
    }

    /**
     * This method will create an [Job] which will be the main [Job] of ran instance
     */
    suspend fun start(
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    ): Job {
        val koinApp = KoinApplication.init()
        koinApp.modules(
            module {
                setupDI(config.databaseConfig.database, json)
            }
        )
        GlobalContext.startKoin(koinApp)
        lateinit var behaviourContext: BehaviourContext
        bot.buildBehaviour(scope = scope) {
            behaviourContext = this
            setupBotPlugin(koinApp.koin)
            deleteWebhook()
        }
        return bot.startGettingOfUpdatesByLongPolling(scope = behaviourContext, updatesFilter = behaviourContext)
    }
}
