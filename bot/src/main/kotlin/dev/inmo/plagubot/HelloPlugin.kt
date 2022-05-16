package dev.inmo.plagubot

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.sql.Database
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module

@Serializable
@SerialName("Hello")
class HelloPlugin : Plugin {
    @Serializable
    data class HelloPluginConfig(
        val print: String
    )
    override suspend fun BehaviourContext.invoke(
        database: Database,
        params: JsonObject
    ) {
        loadModule {
            single {
                get<Json>().decodeFromJsonElement(HelloPluginConfig.serializer(), params["helloPlugin"] ?: return@single null)
            }
        }

        println(get<HelloPluginConfig>().print)
    }
}
