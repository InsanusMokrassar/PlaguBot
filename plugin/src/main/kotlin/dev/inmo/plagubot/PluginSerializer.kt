package dev.inmo.plagubot

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

private val defaultJson = Json {
    ignoreUnknownKeys = true
}

@Serializer(Plugin::class)
object PluginSerializer : KSerializer<Plugin> {
    private val polymorphic = PolymorphicSerializer(Plugin::class)
    override val descriptor: SerialDescriptor = JsonObject.serializer().descriptor

    @InternalSerializationApi
    override fun deserialize(decoder: Decoder): Plugin {
        val format = (decoder as? JsonDecoder) ?.json ?: defaultJson
        val asJson = JsonElement.serializer().deserialize(decoder)
        val jsonObject = (asJson as? JsonObject)

        val type = (jsonObject ?.get("type") as? JsonPrimitive) ?.contentOrNull
        val external = if (type != null) {
            try {
                Class.forName(type) ?.kotlin ?.serializerOrNull()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        return if (jsonObject != null && external != null) {
            format.decodeFromJsonElement(
                external as KSerializer<Plugin>,
                JsonObject(jsonObject.toMutableMap().also { it.remove("type") })
            )
        } else {
            format.decodeFromJsonElement(
                polymorphic,
                asJson
            )
        }
    }

    override fun serialize(encoder: Encoder, value: Plugin) {
        polymorphic.serialize(encoder, value)
    }
}