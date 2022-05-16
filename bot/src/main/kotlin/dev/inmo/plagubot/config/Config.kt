package dev.inmo.plagubot.config

import dev.inmo.plagubot.Plugin
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val botToken: String,
    val plugins: List<Plugin>,
    val database: DatabaseConfig = DatabaseConfig(),
)
