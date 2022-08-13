package dev.inmo.plagubot.fsm

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializer(FSMPlugin::class)
class FSMPluginSerializer : KSerializer<FSMPlugin> {
    override val descriptor: SerialDescriptor
        get() = String.serializer().descriptor

    override fun deserialize(decoder: Decoder): FSMPlugin {
        val kclass = Class.forName(decoder.decodeString()).kotlin
        return (kclass.objectInstance ?: kclass.constructors.first { it.parameters.isEmpty() }.call()) as FSMPlugin
    }

    override fun serialize(encoder: Encoder, value: FSMPlugin) {
        encoder.encodeString(
            value::class.java.canonicalName
        )
    }
}
