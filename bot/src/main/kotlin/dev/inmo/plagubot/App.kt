package dev.inmo.plagubot

import dev.inmo.plagubot.config.*
import kotlinx.coroutines.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.jsonObject
import java.io.File

/**
 * This method by default expects one argument in [args] field: path to config
 */
@InternalSerializationApi
suspend fun main(args: Array<String>) {
    val (configPath) = args
    val file = File(configPath)
    val json = defaultJsonFormat.parseToJsonElement(file.readText()).jsonObject
    val config = defaultJsonFormat.decodeFromJsonElement(Config.serializer(), json)

    PlaguBot(json, config).start().join()
}
