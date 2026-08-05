package io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudProperty
import kotlinx.serialization.Serializable
import org.joml.Vector2f
import org.joml.Vector2i

@Serializable
data class HudLayoutSpec(
    @HudProperty(nameKey = "dynamicrider.hud.editor.layout.screen_anchor")
    val screenAnchor: HudAnchor = HudAnchor.TOP_LEFT,
    @HudProperty(nameKey = "dynamicrider.hud.editor.layout.element_anchor")
    val elementAnchor: HudAnchor = HudAnchor.TOP_LEFT,
    @HudProperty(nameKey = "dynamicrider.hud.editor.layout.scale_x")
    val scaleX: Float = 1f,
    @HudProperty(nameKey = "dynamicrider.hud.editor.layout.scale_y")
    val scaleY: Float = 1f,
    @HudProperty(nameKey = "dynamicrider.hud.editor.layout.x")
    val x: Int = 0,
    @HudProperty(nameKey = "dynamicrider.hud.editor.layout.y")
    val y: Int = 0,
    @HudProperty(nameKey = "dynamicrider.hud.editor.layout.z_index")
    val zIndex: Float = 0f,
) {
    fun toScale(): Vector2f = Vector2f(scaleX, scaleY)

    fun toPosition(): Vector2i = Vector2i(x, y)
}
