package dev.inmo.plagubot.config

import kotlinx.serialization.json.Json

val defaultJsonFormat = Json {
    ignoreUnknownKeys = true
}
