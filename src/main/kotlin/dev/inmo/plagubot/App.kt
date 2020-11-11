package dev.inmo.plagubot

import dev.inmo.micro_utils.coroutines.safelyWithoutExceptions
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.api.telegramBot
import dev.inmo.tgbotapi.extensions.utils.updates.retrieving.startGettingFlowsUpdatesByLongPolling
import dev.inmo.tgbotapi.types.botCommandsLimit
import kotlinx.coroutines.*
import java.io.File

/**
 * This method by default expects one argument in [args] field: path to config
 */
suspend fun main(args: Array<String>) {
    val (configPath) = args
    val file = File(configPath)
    val config = configSerialFormat.decodeFromString(Config.serializer(), file.readText())

    val scope = CoroutineScope(Dispatchers.Default)

    val bot = telegramBot(config.botToken)

    bot.startGettingFlowsUpdatesByLongPolling(scope = scope) {
        val commands = config.plugins.flatMap {
            it.invoke(bot, config.database.database, this, scope)
            it.getCommands()
        }.let {
            val futureUnavailable = it.drop(botCommandsLimit.last)
            if (futureUnavailable.isNotEmpty()) {
                println("Next commands are out of range in setting command request and will be unavailable from autocompleting: ${futureUnavailable}")
            }
            it.take(botCommandsLimit.last)
        }
        scope.launch {
            safelyWithoutExceptions {
                bot.setMyCommands(commands)
            }
        }
    }

}
