package dev.inmo.plagubot

import dev.inmo.plagubot.config.*
import kotlinx.coroutines.*
import kotlinx.serialization.InternalSerializationApi
import java.io.File

@Deprecated(
    "This method is redundant due to new class PlaguBot",
    ReplaceWith(
        "PlaguBot(config).start(scope)",
        "dev.inmo.plagubot.PlaguBot"
    )
)
suspend inline fun initPlaguBot(
    config: Config,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
): Job = PlaguBot(config).start(scope)

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
