package dev.inmo.plagubot.config

import dev.inmo.plagubot.Plugin
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val botToken: String,
    val plugins: List<Plugin>,
    @SerialName("database")
    val databaseConfig: DatabaseConfig = DatabaseConfig(),
)
