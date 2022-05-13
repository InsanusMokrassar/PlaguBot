package dev.inmo.plagubot.config

import com.github.matfax.klassindex.KlassIndex
import dev.inmo.plagubot.Plugin
import dev.inmo.sdi.Module
import dev.inmo.sdi.ModuleSerializer
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
internal val configAndPluginsConfigJsonFormat: Json
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

internal interface PluginsConfiguration {
    val plugins: List<Plugin>
    val params: Module?
}

@Serializable
data class SimplePluginsConfiguration(
    override val plugins: List<@Contextual Plugin>,
    @Contextual
    override val params: Module? = null
) : PluginsConfiguration


internal val DefaultModuleSerializer = ModuleSerializer(emptyList()) {

}

@Serializer(Plugin::class)
internal class InternalPluginSerializer(
    private val params: Module
) : KSerializer<Plugin> {
    override val descriptor: SerialDescriptor = PluginSerializer.descriptor

    @OptIn(InternalSerializationApi::class)
    override fun deserialize(decoder: Decoder): Plugin {
        val asJson = JsonElement.serializer().deserialize(decoder)

        return if (asJson is JsonPrimitive) {
            params[asJson.jsonPrimitive.content] as Plugin
        } else {
            val jsonFormat = ((decoder as? JsonDecoder)?.json ?: configAndPluginsConfigJsonFormat)
            jsonFormat.decodeFromJsonElement(PluginSerializer, asJson)
        }
    }

    @OptIn(InternalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Plugin) {
        params.keys.firstOrNull { params[it] === value } ?.also {
            encoder.encodeString(it)
        } ?: PluginSerializer.serialize(encoder, value)
    }
}

@Serializer(Module::class)
internal class InternalModuleSerializer(
    private val original: JsonElement?,
    private val params: Module
) : KSerializer<Module> {
    override val descriptor: SerialDescriptor = PluginSerializer.descriptor

    @OptIn(InternalSerializationApi::class)
    override fun deserialize(decoder: Decoder): Module {
        val asJson = JsonElement.serializer().deserialize(decoder)

        return if (asJson == original) {
            params
        } else {
            configAndPluginsConfigJsonFormat.decodeFromJsonElement(DefaultModuleSerializer, asJson)
        }
    }

    @OptIn(InternalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: Module) = DefaultModuleSerializer.serialize(encoder, value)
}

internal fun internalPluginSerializerSerializersModule(
    internalPluginSerializer: InternalPluginSerializer,
    internalModuleSerializer: InternalModuleSerializer?
) = SerializersModule {
    contextual(internalPluginSerializer)
    contextual(internalModuleSerializer ?: return@SerializersModule)
}

@Serializer(PluginsConfiguration::class)
internal object PluginsConfigurationSerializer : KSerializer<PluginsConfiguration> {
    private val jsonSerializer = JsonObject.serializer()
    private val moduleSerializer = ModuleSerializer()
    override val descriptor: SerialDescriptor = jsonSerializer.descriptor

    @OptIn(InternalSerializationApi::class)
    override fun deserialize(decoder: Decoder): PluginsConfiguration {
        val json = jsonSerializer.deserialize(decoder)
        val jsonFormat = (decoder as? JsonDecoder) ?.json ?: configAndPluginsConfigJsonFormat
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
        return try {
            resultJsonFormat.decodeFromJsonElement(
                Config.serializer(),
                json
            )
        } catch (e: SerializationException) {
            resultJsonFormat.decodeFromJsonElement(
                SimplePluginsConfiguration.serializer(),
                json
            )
        }
    }

    @OptIn(InternalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: PluginsConfiguration) {
        val params = value.params
        val serializer = when (value) {
            is Config -> Config.serializer()
            is SimplePluginsConfiguration -> SimplePluginsConfiguration.serializer()
            else -> return
        }
        if (params != null) {
            val pluginsSerializer = InternalPluginSerializer(params)

            val jsonFormat = Json(configAndPluginsConfigJsonFormat) {
                serializersModule = encoder.serializersModule.overwriteWith(
                    internalPluginSerializerSerializersModule(pluginsSerializer, null)
                )
            }

            jsonSerializer.serialize(
                encoder,
                when (value) {
                    is Config -> jsonFormat.encodeToJsonElement(Config.serializer(), value)
                    is SimplePluginsConfiguration -> jsonFormat.encodeToJsonElement(SimplePluginsConfiguration.serializer(), value)
                    else -> return
                } as JsonObject
            )
        } else {
            when (value) {
                is Config -> Config.serializer().serialize(encoder, value)
                is SimplePluginsConfiguration -> SimplePluginsConfiguration.serializer().serialize(encoder, value)
                else -> return
            }
        }
    }
}
