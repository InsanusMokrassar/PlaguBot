package dev.inmo.plagubot

import dev.inmo.kslog.common.*
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.module.Module
import org.koin.dsl.module

@Serializable
@SerialName("Hello")
object HelloPlugin : Plugin {
    @Serializable
    data class HelloPluginConfig(
        val print: String
    )

    override fun Module.setupDI(database: Database, params: JsonObject) {
        single {
            get<Json>().decodeFromJsonElement(HelloPluginConfig.serializer(), params["helloPlugin"] ?: return@single null)
        }
    }

    override suspend fun BehaviourContext.setupBotPlugin(koin: Koin) {
        logger.d { koin.get<HelloPluginConfig>().print }
        logger.dS { getMe().toString() }
        onCommand("hello_world") {
            reply(it, "Hello :)")
        }
    }
}
