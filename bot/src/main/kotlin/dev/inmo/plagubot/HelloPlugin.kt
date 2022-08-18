package dev.inmo.plagubot

import dev.inmo.kslog.common.*
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.plagubot.HelloPlugin.setupBotPlugin
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onUnhandledCommand
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

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

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val toPrint = koin.getOrNull<HelloPluginConfig>() ?.print ?: "Hello :)"
        logger.d { toPrint }
        logger.dS { getMe().toString() }
        onCommand("hello_world") {
            reply(it, toPrint)
        }
    }
}
