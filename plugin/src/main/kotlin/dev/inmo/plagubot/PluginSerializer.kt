package dev.inmo.plagubot

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(Plugin::class)
class PluginSerializer : KSerializer<Plugin> {
    override val descriptor: SerialDescriptor
        get() = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): Plugin {
        val kclass = Class.forName(decoder.decodeString()).kotlin
        return (kclass.objectInstance ?: kclass.constructors.first { it.parameters.isEmpty() }.call()) as Plugin
    }

    override fun serialize(encoder: Encoder, value: Plugin) {
        encoder.encodeString(
            value::class.java.canonicalName
        )
    }
}
