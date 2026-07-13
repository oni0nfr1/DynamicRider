package io.github.oni0nfr1.dynamicrider.client.hud.layout

import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import kotlin.math.max
import kotlin.math.min

data class HudBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    fun contains(x: Float, y: Float): Boolean = x >= left && x <= right && y >= top && y <= bottom
}

data class HudLayoutResult(
    val renderX: Float,
    val renderY: Float,
    val scaleX: Float,
    val scaleY: Float,
    val zIndex: Float,
    val bounds: HudBounds,
) {
    fun toLocal(screenX: Float, screenY: Float): Pair<Float, Float>? {
        if (scaleX == 0f || scaleY == 0f) return null
        return (screenX - renderX) / scaleX to (screenY - renderY) / scaleY
    }
}

object HudLayoutEngine {
    fun resolve(
        layout: HudLayoutSpec,
        parentWidth: Int,
        parentHeight: Int,
        elementWidth: Int,
        elementHeight: Int,
    ): HudLayoutResult {
        val parentPoint = layout.screenAnchor.point(parentWidth, parentHeight)
        val elementPoint = layout.elementAnchor.point(elementWidth, elementHeight)
        val renderX = layout.x + parentPoint.x - elementPoint.x * layout.scaleX
        val renderY = layout.y + parentPoint.y - elementPoint.y * layout.scaleY
        val oppositeX = renderX + elementWidth * layout.scaleX
        val oppositeY = renderY + elementHeight * layout.scaleY
        return HudLayoutResult(
            renderX = renderX,
            renderY = renderY,
            scaleX = layout.scaleX,
            scaleY = layout.scaleY,
            zIndex = layout.zIndex,
            bounds = HudBounds(
                left = min(renderX, oppositeX),
                top = min(renderY, oppositeY),
                right = max(renderX, oppositeX),
                bottom = max(renderY, oppositeY),
            ),
        )
    }
}
