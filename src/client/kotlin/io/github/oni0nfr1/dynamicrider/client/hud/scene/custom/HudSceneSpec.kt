package io.github.oni0nfr1.dynamicrider.client.hud.scene.custom

import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
enum class HudSceneMode {
    RIDE,
    SPECTATE,
}

@Serializable
data class HudSceneSpec(
    val elements: List<@Polymorphic HudElementSpec<*, *>>,
)
