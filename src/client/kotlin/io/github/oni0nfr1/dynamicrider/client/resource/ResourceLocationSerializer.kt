package io.github.oni0nfr1.dynamicrider.client.resource

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.resources.ResourceLocation

object ResourceLocationSerializer : KSerializer<ResourceLocation> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            serialName = "ResourceLocation",
            kind = PrimitiveKind.STRING
        )

    override fun serialize(
        encoder: Encoder,
        value: ResourceLocation
    ) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(
        decoder: Decoder
    ): ResourceLocation {
        val resourceLocationString = decoder.decodeString()

        return ResourceLocation.tryParse(resourceLocationString)
            ?: throw SerializationException(
                "Invalid ResourceLocation: $resourceLocationString"
            )
    }
}