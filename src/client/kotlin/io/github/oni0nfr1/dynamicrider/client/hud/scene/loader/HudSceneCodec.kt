package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object HudSceneCodec {
    private val json = Json {
        serializersModule = HudElementTypeRegistry.serializersModule
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    /**
     * JSON 문자열을 현재 포맷의 HUD 장면 명세로 역직렬화하고 구조를 검증한다.
     *
     * @param content 역직렬화할 JSON 문자열
     * @return 검증을 통과한 HUD 장면 명세
     * @throws SerializationException JSON이 잘못되었거나 포맷 버전 또는 요소 ID가 유효하지 않은 경우
     */
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

    /**
     * HUD 장면 명세를 config 파일에 저장할 JSON 문자열로 직렬화한다.
     *
     * @param spec 직렬화할 HUD 장면 명세
     * @return 기본값과 타입 판별자를 포함한 JSON 문자열
     */
    fun encode(spec: HudSceneSpec): String = json.encodeToString(spec)
}
