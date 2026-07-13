package io.github.oni0nfr1.dynamicrider.client.hud.scene.custom

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object HudSceneCodec {
    private val json = Json {
        serializersModule = HudElementSerializersModule
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    fun decode(content: String): HudSceneSpec {
        val spec = json.decodeFromString<HudSceneSpec>(content)
        if (spec.formatVersion != HudSceneSpec.CURRENT_FORMAT_VERSION) {
            throw SerializationException(
                "Unsupported HUD scene format version ${spec.formatVersion}; " +
                    "expected ${HudSceneSpec.CURRENT_FORMAT_VERSION}"
            )
        }
        if (spec.elementIds.isNotEmpty()) {
            if (spec.elementIds.size != spec.elements.size) {
                throw SerializationException("elementIds must have the same size as elements")
            }
            if (spec.elementIds.any(String::isBlank) || spec.elementIds.distinct().size != spec.elementIds.size) {
                throw SerializationException("elementIds must be non-blank and unique")
            }
        }
        return spec
    }

    fun encode(spec: HudSceneSpec): String = json.encodeToString(spec)
}
