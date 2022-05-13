package dev.inmo.plagubot.config

import dev.inmo.plagubot.Plugin
import dev.inmo.sdi.Module
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class Config(
    override val plugins: List<Plugin>,
    val database: DatabaseConfig = DatabaseConfig(),
    val botToken: String,
    val rawConfig: JsonObject
) : PluginsConfiguration
