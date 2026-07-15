package io.github.oni0nfr1.dynamicrider.client.hud.scene.model

import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
enum class HudSceneMode {
    RIDE,
    SPECTATE,
}

/** 직렬화 가능한 HUD 장면의 요소 ID, 순서와 불변 요소 명세다. */
@Serializable
data class HudSceneSpec(
    val formatVersion: Int = CURRENT_FORMAT_VERSION,
    val elementIds: List<String> = emptyList(),
    val elements: List<@Polymorphic HudElementSpec<*, *>>,
) {
    companion object {
        const val CURRENT_FORMAT_VERSION: Int = 1
    }
}
