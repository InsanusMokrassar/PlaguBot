package dev.inmo.plagubot

import dev.inmo.micro_utils.coroutines.safelyWithoutExceptions
import dev.inmo.plagubot.config.Config
import dev.inmo.plagubot.config.configSerialFormat
import dev.inmo.tgbotapi.bot.Ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviour
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.longPolling
import dev.inmo.tgbotapi.types.botCommandsLimit
import kotlinx.coroutines.*
import kotlinx.serialization.InternalSerializationApi
import java.io.File

suspend inline fun initPlaguBot(
    config: Config,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    val bot = telegramBot(config.botToken)

    bot.buildBehaviour(scope) {
        val commands = config.plugins.flatMap {
            it.apply { invoke(config.database.database) }
            it.getCommands()
        }.let {
            val futureUnavailable = it.drop(botCommandsLimit.last)
            if (futureUnavailable.isNotEmpty()) {
                println("Next commands are out of range in setting command request and will be unavailable from autocompleting: ${futureUnavailable}")
            }
            it.take(botCommandsLimit.last)
        }
        safelyWithoutExceptions { setMyCommands(commands) }
    }
}

/**
 * This method by default expects one argument in [args] field: path to config
 */
@InternalSerializationApi
suspend fun main(args: Array<String>) {
    val (configPath) = args
    val file = File(configPath)
    val config = configSerialFormat.decodeFromString(Config.serializer(), file.readText())

    val scope = CoroutineScope(Dispatchers.Default)
    initPlaguBot(config, scope)
    scope.coroutineContext.job.join()
}
