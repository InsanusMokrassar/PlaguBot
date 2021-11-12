package dev.inmo.plagubot

import dev.inmo.plagubot.config.*
import kotlinx.coroutines.*
import kotlinx.serialization.InternalSerializationApi
import java.io.File

/**
 * This method by default expects one argument in [args] field: path to config
 */
@InternalSerializationApi
suspend fun main(args: Array<String>) {
    val (configPath) = args
    val file = File(configPath)
    val config = configAndPluginsConfigJsonFormat.decodeFromString(PluginsConfigurationSerializer, file.readText()) as Config

    PlaguBot(config).start().join()
}
