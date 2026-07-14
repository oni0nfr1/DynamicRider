package io.github.oni0nfr1.dynamicrider.client.hud.layout

import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import kotlin.math.max
import kotlin.math.min

/** 화면 좌표계에서 HUD 요소가 차지하는 축 정렬 영역이다. */
data class HudBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top

    /** 주어진 화면 좌표가 경계를 포함한 영역 안에 있는지 확인한다. */
    fun contains(x: Float, y: Float): Boolean = x >= left && x <= right && y >= top && y <= bottom
}

/** 계산이 완료된 HUD 요소의 렌더 transform과 화면 영역이다. */
data class HudLayoutResult(
    val renderX: Float,
    val renderY: Float,
    val scaleX: Float,
    val scaleY: Float,
    val zIndex: Float,
    val bounds: HudBounds,
) {
    /**
     * 화면 좌표를 요소의 scale 적용 전 로컬 좌표로 변환한다.
     *
     * @return 변환된 `(x, y)`, scale 축 중 하나가 0이면 `null`
     */
    fun toLocal(screenX: Float, screenY: Float): Pair<Float, Float>? {
        if (scaleX == 0f || scaleY == 0f) return null
        return (screenX - renderX) / scaleX to (screenY - renderY) / scaleY
    }
}

/** HUD anchor와 layout 명세를 실제 화면 좌표로 변환한다. */
object HudLayoutEngine {
    /**
     * 부모 및 요소 크기를 기준으로 렌더 원점, scale, z-index와 hit-test 영역을 계산한다.
     *
     * 음수 scale도 허용하며 [HudBounds]는 항상 정규화된 좌표로 반환한다.
     */
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
