package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec

@HUDSL
class HudLayoutBuilder {
    var screenAnchor: HudAnchor = HudAnchor.TOP_LEFT
    var elementAnchor: HudAnchor = HudAnchor.TOP_LEFT
    var scaleX: Float = 1f
    var scaleY: Float = 1f
    var x: Int = 0
    var y: Int = 0
    var zIndex: Float = 0f

    fun build(): HudLayoutSpec = HudLayoutSpec(
        screenAnchor = screenAnchor,
        elementAnchor = elementAnchor,
        scaleX = scaleX,
        scaleY = scaleY,
        x = x,
        y = y,
        zIndex = zIndex,
    )
}
