package dev.inmo.plagubot.config

import dev.inmo.plagubot.Plugin
import dev.inmo.sdi.Module
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    override val plugins: List<@Contextual Plugin>,
    val database: DatabaseConfig = DatabaseConfig(),
    val botToken: String,
    @Contextual
    override val params: Module? = null
) : PluginsConfiguration
