package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import kotlin.math.min

/** 고정된 HUD 논리 좌표와 화면의 preview 표시 영역 사이를 변환한다. */
class HudPreviewTransform private constructor(
    val logicalWidth: Int,
    val logicalHeight: Int,
    val displayX: Float,
    val displayY: Float,
    val displayWidth: Float,
    val displayHeight: Float,
    val scale: Float,
    val contentX: Float,
    val contentY: Float,
    val contentWidth: Float,
    val contentHeight: Float,
) {
    data class Point(val x: Float, val y: Float)

    fun logicalToScreen(x: Float, y: Float): Point = Point(
        x = contentX + x * scale,
        y = contentY + y * scale,
    )

    fun screenToLogical(x: Double, y: Double): Point = Point(
        x = ((x.toFloat() - contentX) / scale),
        y = ((y.toFloat() - contentY) / scale),
    )

    fun containsScreenPoint(x: Double, y: Double): Boolean =
        x >= contentX && x < contentX + contentWidth &&
            y >= contentY && y < contentY + contentHeight

    companion object {
        fun fit(
            logicalWidth: Int,
            logicalHeight: Int,
            displayX: Float,
            displayY: Float,
            displayWidth: Float,
            displayHeight: Float,
        ): HudPreviewTransform {
            val safeLogicalWidth = logicalWidth.coerceAtLeast(1)
            val safeLogicalHeight = logicalHeight.coerceAtLeast(1)
            val safeDisplayWidth = displayWidth.coerceAtLeast(1f)
            val safeDisplayHeight = displayHeight.coerceAtLeast(1f)
            val scale = min(
                safeDisplayWidth / safeLogicalWidth,
                safeDisplayHeight / safeLogicalHeight,
            )
            val contentWidth = safeLogicalWidth * scale
            val contentHeight = safeLogicalHeight * scale
            return HudPreviewTransform(
                logicalWidth = safeLogicalWidth,
                logicalHeight = safeLogicalHeight,
                displayX = displayX,
                displayY = displayY,
                displayWidth = safeDisplayWidth,
                displayHeight = safeDisplayHeight,
                scale = scale,
                contentX = displayX + (safeDisplayWidth - contentWidth) / 2f,
                contentY = displayY + (safeDisplayHeight - contentHeight) / 2f,
                contentWidth = contentWidth,
                contentHeight = contentHeight,
            )
        }
    }
}
