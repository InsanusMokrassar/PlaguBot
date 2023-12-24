package dev.inmo.plagubot.config

import dev.inmo.micro_utils.common.Warning
import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.utils.telegramBotAPIDefaultUrl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Warning("This API is internal and can be changed without notifications of mentions of changes")
@Serializable
data class Config(
    val botToken: String,
    val plugins: List<Plugin>,
    @SerialName("database")
    val databaseConfig: DatabaseConfig = DatabaseConfig(),
    val botApiServer: String = telegramBotAPIDefaultUrl
)
