package dev.inmo.plagubot

import dev.inmo.plagubot.config.*
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.*
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.startGettingOfUpdatesByLongPolling
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.KoinApplication
import org.koin.core.component.get
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val DefaultPlaguBotParamsKey = "plagubot"
val Plugin.plagubot: PlaguBot
    get() = get()

@Serializable
data class PlaguBot(
    private val json: JsonObject,
    private val config: Config
) : Plugin {
    @Transient
    private val bot = telegramBot(config.botToken)

    override suspend fun BehaviourContext.invoke(
        database: Database,
        params: JsonObject
    ) {
        config.plugins.forEach {
            it.apply { invoke(database, params) }
        }
    }

    /**
     * This method will create an [Job] which will be the main [Job] of ran instance
     */
    suspend fun start(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    ): Job {
        val koinApp = KoinApplication.init()
        koinApp.modules(
            module {
                single { config }
                single { config.plugins }
                single { config.database }
                single(named(defaultDatabaseParamsName)) { config.database.database }
                single { defaultJsonFormat }
                single(named(DefaultPlaguBotParamsKey)) { this@PlaguBot }
            }
        )
        lateinit var behaviourContext: BehaviourContext
        bot.buildBehaviour(scope = scope) {
            invoke(config.database.database, json)
            behaviourContext = this
        }
        GlobalContext.startKoin(koinApp)
        return bot.startGettingOfUpdatesByLongPolling(scope = behaviourContext, updatesFilter = behaviourContext)
    }
}
