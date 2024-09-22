package dev.inmo.plagubot.config

import dev.inmo.micro_utils.common.Warning
import kotlinx.serialization.json.Json

@Warning("This format will not be configured throw StartPlugin system. Use it will caution, it has no any configured things")
val defaultJsonFormat = Json {
    ignoreUnknownKeys = true
}
