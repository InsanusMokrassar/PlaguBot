package dev.inmo.plagubot.config

import com.github.matfax.klassindex.KlassIndex
import dev.inmo.plagubot.Plugin
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*
import kotlin.reflect.KClass

@InternalSerializationApi
internal inline fun <T : Plugin> KClass<T>.includeIn(builder: PolymorphicModuleBuilder<Plugin>) = builder.subclass(this, serializer())

@InternalSerializationApi
internal val configSerialFormat: StringFormat
    get() = Json {
        ignoreUnknownKeys = true
        serializersModule = SerializersModule {
            polymorphic(Plugin::class) {
                KlassIndex.getSubclasses(Plugin::class).flatMap { kclass ->
                    kclass.includeIn(this)
                    kclass.annotations.mapNotNull { it as? SerialName }.map {
                        it.value to kclass.serializer()
                    } + listOfNotNull(
                        kclass.simpleName ?.let {
                            it to kclass.serializer()
                        }
                    )
                }.toMap().let {
                    default { requiredType ->
                        it[requiredType]
                    }
                }
            }
        }
    }

@Serializable
data class Config(
    val plugins: List<@Contextual Plugin>,
    val database: DatabaseConfig,
    val botToken: String
)
