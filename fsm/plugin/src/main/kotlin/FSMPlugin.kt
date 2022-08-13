package dev.inmo.plagubot.fsm

import dev.inmo.plagubot.Plugin
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContextWithFSM
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.module.Module

/**
 * **ANY REALIZATION OF [FSMPlugin] MUST HAVE CONSTRUCTOR WITH ABSENCE OF INCOMING PARAMETERS**
 *
 * Use this interface for your bot. It is possible to use [kotlinx.serialization.SerialName] annotations on your plugins
 * to set up short name for your plugin. Besides, simple name of your class will be used as key for deserialization
 * too.
 */
@Serializable(FSMPluginSerializer::class)
interface FSMPlugin : Plugin {
    suspend fun BehaviourContextWithFSM<*>.setupBotPlugin(koin: Koin) {
        (this as BehaviourContext).setupBotPlugin(koin)
    }
}
