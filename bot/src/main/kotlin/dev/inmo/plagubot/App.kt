package dev.inmo.plagubot

import dev.inmo.kslog.common.*
import dev.inmo.plagubot.config.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.jsonObject
import java.io.File

/**
 * This method by default expects one argument in [args] field: path to config
 */
@InternalSerializationApi
suspend fun main(args: Array<String>) {
    KSLog.default = KSLog("PlaguBot")
    val (configPath) = args
    val file = File(configPath)
    KSLog.i("Start read config from ${file.absolutePath}")
    val json = defaultJsonFormat.parseToJsonElement(file.readText()).jsonObject
    val config = defaultJsonFormat.decodeFromJsonElement(Config.serializer(), json)
    KSLog.i("Config has been read")

    PlaguBot(json, config).start().join()
}
