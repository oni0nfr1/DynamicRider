package io.github.oni0nfr1.dynamicrider.client.hud.elements.spec

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import kotlinx.serialization.Serializable
import org.joml.Vector2f
import org.joml.Vector2i

@Serializable
data class HudLayoutSpec(
    val screenAnchor: HudAnchor = HudAnchor.TOP_LEFT,
    val elementAnchor: HudAnchor = HudAnchor.TOP_LEFT,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val x: Int = 0,
    val y: Int = 0,
    val zIndex: Float = 0f,
) {
    fun toScale(): Vector2f = Vector2f(scaleX, scaleY)

    fun toPosition(): Vector2i = Vector2i(x, y)
}
