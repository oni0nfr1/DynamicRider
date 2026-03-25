package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import org.joml.Vector2f
import org.joml.Vector2i

data class ElementLayoutBuilder(
    var screenAnchor: HudAnchor = HudAnchor.TOP_LEFT,
    var elementAnchor: HudAnchor = HudAnchor.TOP_LEFT,
    var scale: Vector2f = Vector2f(1f),
    var position: Vector2i = Vector2i(0),
    var zIndex: Float = 0f,
)