package dev.inmo.plagubot.config

import dev.inmo.micro_utils.common.Warning
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.utils.telegramBotAPIDefaultUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Warning("This API is internal and can be changed without notifications or mentions of changes")
@Serializable
data class Config(
    val botToken: String,
    val plugins: List<StartPlugin>,
    @SerialName("database")
    val databaseConfig: DatabaseConfig = DatabaseConfig(),
    val botApiServer: String = telegramBotAPIDefaultUrl,
    val testServer: Boolean = false
) {
    val botPlugins = plugins.filterIsInstance<Plugin>()
}
