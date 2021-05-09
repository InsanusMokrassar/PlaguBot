package dev.inmo.plagubot

import dev.inmo.plagubot.config.PluginsConfigurationSerializer
import dev.inmo.plagubot.config.SimplePluginsConfiguration
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.BotCommand
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database

@Serializable
data class PluginsHolder(
    @Serializable(PluginsConfigurationSerializer::class)
    private val pluginsConfiguration: SimplePluginsConfiguration
) : Plugin {
    override suspend fun getCommands(): List<BotCommand> = pluginsConfiguration.plugins.flatMap {
        it.getCommands()
    }

    override suspend fun BehaviourContext.invoke(database: Database, params: Map<String, Any>) {
        val finalParams = pluginsConfiguration.params ?.plus(params) ?: params
        pluginsConfiguration.plugins.forEach {
            it.apply { invoke(database, finalParams) }
        }
    }
}