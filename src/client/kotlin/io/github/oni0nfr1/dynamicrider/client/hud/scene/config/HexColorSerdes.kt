package io.github.oni0nfr1.dynamicrider.client.hud.scene.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object HexColorSerdes : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ArgbColorAsHex", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeString("#${value.toUInt().toString(16).uppercase().padStart(8, '0')}")
    }

    override fun deserialize(decoder: Decoder): Int {
        val raw = decoder.decodeString().trim()
        val hex = raw.removePrefix("#").removePrefix("0x").removePrefix("0X")
        require(hex.length == 6 || hex.length == 8) {
            "Color must be #RRGGBB or #AARRGGBB: $raw"
        }

        val normalized = if (hex.length == 6) "FF$hex" else hex
        return normalized.toUInt(16).toInt()
    }
}

