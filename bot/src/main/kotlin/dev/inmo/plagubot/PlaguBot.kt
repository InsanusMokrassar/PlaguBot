package dev.inmo.plagubot

import dev.inmo.micro_utils.coroutines.safelyWithoutExceptions
import dev.inmo.plagubot.config.*
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.*
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.types.botCommandsLimit
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.sql.Database

const val DefaultPlaguBotParamsKey = "plagubot"
val Map<String, Any>.plagubot
    get() = get(DefaultPlaguBotParamsKey) as? PlaguBot

@Serializable
data class PlaguBot(
    @Serializable(PluginsConfigurationSerializer::class)
    private val config: Config
) : Plugin {
    @Transient
    private val bot = telegramBot(config.botToken)
    @Transient
    private val database = config.params ?.database ?: config.database.database

    override suspend fun getCommands(): List<BotCommand> = config.plugins.flatMap {
        it.getCommands()
    }

    override suspend fun BehaviourContext.invoke(database: Database, params: Map<String, Any>) {
        config.plugins.forEach {
            it.apply { invoke(database, params) }
        }
        val commands = getCommands()
        val futureUnavailable = commands.drop(botCommandsLimit.last)
        if (futureUnavailable.isNotEmpty()) {
            println("Next commands are out of range in setting command request and will be unavailable from autocompleting: $futureUnavailable")
        }
        safelyWithoutExceptions { setMyCommands(commands.take(botCommandsLimit.last)) }
    }

    /**
     * This method will create an [Job] which will be the main [Job] of ran instance
     */
    suspend fun start(
        scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    ): Job = bot.buildBehaviourWithLongPolling(scope) {
        invoke(database, paramsMap)
    }
}
