package dev.inmo.plagubot.config

import com.github.matfax.klassindex.KlassIndex
import dev.inmo.plagubot.Plugin
import dev.inmo.plagubot.PluginSerializer
import dev.inmo.sdi.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*
import kotlin.reflect.KClass

@InternalSerializationApi
internal inline fun <T : Plugin> KClass<T>.includeIn(builder: PolymorphicModuleBuilder<Plugin>) = builder.subclass(this, serializer())

@InternalSerializationApi
internal val configJsonFormat: Json
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
    val database: DatabaseConfig = DatabaseConfig(),
    val botToken: String,
    @Contextual
    val params: Module? = null
)

@Serializer(Plugin::class)
private class InternalPluginSerializer(
    private val params: Module
) : KSerializer<Plugin> {
    override val descriptor: SerialDescriptor = PluginSerializer.descriptor

    @InternalSerializationApi
    override fun deserialize(decoder: Decoder): Plugin {
        val asJson = JsonElement.serializer().deserialize(decoder)

        return if (asJson is JsonPrimitive) {
            params[asJson.jsonPrimitive.content] as Plugin
        } else {
            val jsonFormat = ((decoder as? JsonDecoder)?.json ?: configJsonFormat)
            jsonFormat.decodeFromJsonElement(PluginSerializer, asJson)
        }
    }

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: Plugin) {
        params.keys.firstOrNull { params[it] === value } ?.also {
            encoder.encodeString(it)
        } ?: PluginSerializer.serialize(encoder, value)
    }
}

private val DefaultModuleSerializer = ModuleSerializer(emptyList()) {

}

@Serializer(Module::class)
private class InternalModuleSerializer(
    private val original: JsonElement?,
    private val params: Module
) : KSerializer<Module> {
    override val descriptor: SerialDescriptor = PluginSerializer.descriptor

    @InternalSerializationApi
    override fun deserialize(decoder: Decoder): Module {
        val asJson = JsonElement.serializer().deserialize(decoder)

        return if (asJson == original) {
            params
        } else {
            configJsonFormat.decodeFromJsonElement(DefaultModuleSerializer, asJson)
        }
    }

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: Module) = DefaultModuleSerializer.serialize(encoder, value)
}

private fun internalPluginSerializerSerializersModule(
    internalPluginSerializer: InternalPluginSerializer,
    internalModuleSerializer: InternalModuleSerializer?
) = SerializersModule {
    contextual(internalPluginSerializer)
    contextual(internalModuleSerializer ?: return@SerializersModule)
}

@Serializer(Config::class)
internal object ConfigSerializer : KSerializer<Config> {
    private val jsonSerializer = JsonObject.serializer()
    private val moduleSerializer = ModuleSerializer()

    @InternalSerializationApi
    override fun deserialize(decoder: Decoder): Config {
        val json = jsonSerializer.deserialize(decoder)
        val jsonFormat = (decoder as? JsonDecoder) ?.json ?: configJsonFormat
        val paramsRow = json["params"]

        val resultJsonFormat = if (paramsRow != null && paramsRow != JsonNull) {
            val params = jsonFormat.decodeFromJsonElement(
                moduleSerializer,
                paramsRow
            )

            val pluginsSerializer = InternalPluginSerializer(params)
            val moduleSerializer = InternalModuleSerializer(paramsRow, params)
            Json(jsonFormat) {
                serializersModule = decoder.serializersModule.overwriteWith(
                    internalPluginSerializerSerializersModule(pluginsSerializer, moduleSerializer)
                )
            }
        } else {
            jsonFormat
        }
        return resultJsonFormat.decodeFromJsonElement(
            Config.serializer(),
            json
        )
    }

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, value: Config) {
        if (value.params != null) {
            val pluginsSerializer = InternalPluginSerializer(value.params)

            val jsonFormat = Json(configJsonFormat) {
                serializersModule = encoder.serializersModule.overwriteWith(
                    internalPluginSerializerSerializersModule(pluginsSerializer, null)
                )
            }

            jsonSerializer.serialize(
                encoder,
                jsonFormat.encodeToJsonElement(
                    Config.serializer(),
                    value
                ) as JsonObject
            )
        } else {
            Config.serializer().serialize(encoder, value)
        }
    }
}
