package dev.inmo.plagubot

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.updateshandlers.FlowsUpdatesFilter
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.exposed.sql.Database

interface Plugin {
    suspend fun getCommands(): List<BotCommand> = emptyList()
    suspend operator fun invoke(
        bot: TelegramBot,
        database: Database,
        updatesFilter: FlowsUpdatesFilter,
        scope: CoroutineScope
    )
}
