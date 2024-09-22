package dev.inmo.plagubot

import dev.inmo.kslog.common.*
import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.*
import dev.inmo.tgbotapi.extensions.behaviour_builder.expectations.waitTextMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.IdChatIdentifier
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.module.Module

@Serializable
@SerialName("Hello")
object HelloPlugin : Plugin {
    @Serializable
    data class HelloPluginConfig(
        val print: String
    )

    override fun Module.setupDI(config: JsonObject) {
        registerConfig<HelloPluginConfig>("helloPlugin") { null }
    }

    private sealed interface InternalFSMState : State {
        override val context: IdChatIdentifier
        data class DidntSaidHello(override val context: IdChatIdentifier) : InternalFSMState
        data class SaidHelloOnce(override val context: IdChatIdentifier) : InternalFSMState
    }

    override suspend fun startPlugin(koin: Koin) {
        super.startPlugin(koin)
        logger.i { "This logic called BEFORE the bot will be started and setup" }
    }

    override suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        val toPrint = koin.configOrNull<HelloPluginConfig>() ?.print ?: "Hello :)"
        logger.d { toPrint }
        logger.dS { getMe().toString() }
        onCommand("hello_world") {
            startChain(InternalFSMState.DidntSaidHello(it.chat.id))
        }

        strictlyOn { state: InternalFSMState.DidntSaidHello ->
            sendMessage(state.context, toPrint)
            InternalFSMState.SaidHelloOnce(state.context)
        }

        strictlyOn { state: InternalFSMState.SaidHelloOnce ->
            val message = waitTextMessage().first()
            reply(message, "Sorry, I can answer only this: $toPrint")
            InternalFSMState.SaidHelloOnce(state.context)
        }
    }
}
