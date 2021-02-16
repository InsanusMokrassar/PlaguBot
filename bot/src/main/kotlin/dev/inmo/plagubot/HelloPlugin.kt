package dev.inmo.plagubot

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database

@Serializable
@SerialName("Hello")
data class HelloPlugin(
    val parameter: String
) : Plugin {
    override suspend fun BehaviourContext.invoke(database: Database) {
        println(parameter)
    }
}
