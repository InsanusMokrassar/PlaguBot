package dev.inmo.plagubot

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.updateshandlers.FlowsUpdatesFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database

@Serializable
@SerialName("Hello")
data class HelloPlugin(
    val parameter: String
) : Plugin {
    override suspend fun invoke(
        bot: TelegramBot,
        database: Database,
        updatesFilter: FlowsUpdatesFilter,
        scope: CoroutineScope
    ) {
        println(parameter)
    }
}
