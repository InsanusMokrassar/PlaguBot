package dev.inmo.plagubot

import dev.inmo.plagubot.config.DatabaseConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

val configSerialFormat: StringFormat
    get() = Json {
        ignoreUnknownKeys = true
    }

@Serializable
data class Config(
    val plugins: List<@Contextual Plugin>,
    val database: DatabaseConfig,
    val botToken: String
)
