package dev.inmo.plagubot

import dev.inmo.micro_utils.fsm.common.State
import dev.inmo.micro_utils.startup.plugin.StartPlugin
import dev.inmo.tgbotapi.bot.ktor.KtorRequestsExecutorBuilder
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.koin.core.Koin
import org.koin.core.scope.Scope

/**
 * **ANY REALIZATION OF [Plugin] MUST HAVE CONSTRUCTOR WITH ABSENCE OF INCOMING PARAMETERS**
 *
 * Use this interface for your bot. It is possible to use [kotlinx.serialization.SerialName] annotations on your plugins
 * to set up short name for your plugin. Besides, simple name of your class will be used as key for deserialization
 * too.
 */
@Serializable(PluginSerializer::class)
interface Plugin : StartPlugin {
    @Deprecated("Deprecated in favor to setupBotClient with arguments")
    fun KtorRequestsExecutorBuilder.setupBotClient() {}

    /**
     * Will be called on stage of bot setup
     *
     * @param scope The scope of [org.koin.core.module.Module.single] of bot definition
     * @param params Params (in fact, the whole bot config)
     */
    fun KtorRequestsExecutorBuilder.setupBotClient(scope: Scope, params: JsonObject) = setupBotClient()

    /**
     * Override this method in cases when you want to declare common bot behaviour. In case you wish to use FSM, you
     * should override the method with receiver [BehaviourContextWithFSM]
     */
    suspend fun BehaviourContext.setupBotPlugin(
        koin: Koin
    ) {}
    /**
     * Override this method in cases when you want to declare full behaviour of the plugin. It is recommended to declare
     * common logic of plugin in the [setupBotPlugin] with [BehaviourContext] receiver and use override this one
     * for the FSM configuration
     */
    suspend fun BehaviourContextWithFSM<State>.setupBotPlugin(koin: Koin) {
        (this as BehaviourContext).setupBotPlugin(koin)
    }
}
