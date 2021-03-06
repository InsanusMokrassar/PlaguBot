package dev.inmo.plagubot

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.updateshandlers.FlowsUpdatesFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database

/**
 * Use this interface for your bot. It is possible to use [kotlinx.serialization.SerialName] annotations on your plugins
 * to set up short name for your plugin. Besides, simple name of your class will be used as key for deserialization
 * too.
 */
@Serializable(PluginSerializer::class)
interface Plugin {
    /**
     * In case you want to publish some processed by your plugin commands, you can provide it via this method
     *
     * @see BotCommand
     */
    suspend fun getCommands(): List<BotCommand> = emptyList()

    @Deprecated("Override other method with receiver BehaviourContext")
    suspend operator fun invoke(
        bot: TelegramBot,
        database: Database,
        updatesFilter: FlowsUpdatesFilter,
        scope: CoroutineScope
    ) {}

    /**
     * This method (usually) will be invoked just one time in the whole application.
     */
    suspend operator fun BehaviourContext.invoke(
        database: Database
    ) = invoke(bot, database, flowsUpdatesFilter, scope)

    /**
     * This method (usually) will be invoked just one time in the whole application.
     */
    suspend operator fun BehaviourContext.invoke(
        database: Database,
        params: Map<String, Any>
    ) = invoke(database)
}
