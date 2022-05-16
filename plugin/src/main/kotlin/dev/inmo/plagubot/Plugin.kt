package dev.inmo.plagubot

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.BotCommand
import dev.inmo.tgbotapi.updateshandlers.FlowsUpdatesFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.dsl.ModuleDeclaration
import org.koin.dsl.module

/**
 * **ANY REALIZATION OF [Plugin] MUST HAVE CONSTRUCTOR WITH ABSENCE OF INCOMING PARAMETERS**
 *
 * Use this interface for your bot. It is possible to use [kotlinx.serialization.SerialName] annotations on your plugins
 * to set up short name for your plugin. Besides, simple name of your class will be used as key for deserialization
 * too.
 */
@Serializable(PluginSerializer::class)
interface Plugin : KoinComponent {
    fun loadModule(createdAtStart: Boolean = false, moduleDeclaration: ModuleDeclaration) = getKoin().loadModules(
        listOf(
            module(createdAtStart, moduleDeclaration)
        )
    )
    /**
     * This method (usually) will be invoked just one time in the whole application.
     */
    suspend operator fun BehaviourContext.invoke(
        database: Database,
        params: JsonObject
    ) {}
}
