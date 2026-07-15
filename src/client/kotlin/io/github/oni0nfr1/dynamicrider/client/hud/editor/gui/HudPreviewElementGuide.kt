package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.layout.HudBounds
import io.github.oni0nfr1.dynamicrider.client.hud.layout.HudLayoutEngine
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState

/** Preview 좌표계에서 요소 선택과 anchor overlay에 필요한 계산 결과다. */
data class HudPreviewElementGuide(
    val elementId: String,
    val bounds: HudBounds,
    val screenAnchorX: Float,
    val screenAnchorY: Float,
    val elementAnchorX: Float,
    val elementAnchorY: Float,
    val elementAnchor: HudAnchor,
    val scaleX: Float,
    val scaleY: Float,
    val zIndex: Float,
    val renderOrder: Int,
) {
    fun contains(x: Float, y: Float): Boolean = bounds.contains(x, y)

    /** 요소 anchor 종류로 handle 모서리를 고정하며 중앙 축은 큰 좌표 방향을 택한다. */
    fun scaleHandle(): Pair<Float, Float> {
        val x = when (elementAnchor) {
            HudAnchor.TOP_LEFT, HudAnchor.MIDDLE_LEFT, HudAnchor.BOTTOM_LEFT ->
                if (scaleX < 0f) bounds.left else bounds.right
            HudAnchor.TOP_CENTER, HudAnchor.MIDDLE_CENTER, HudAnchor.BOTTOM_CENTER -> bounds.right
            HudAnchor.TOP_RIGHT, HudAnchor.MIDDLE_RIGHT, HudAnchor.BOTTOM_RIGHT ->
                if (scaleX < 0f) bounds.right else bounds.left
        }
        val y = when (elementAnchor) {
            HudAnchor.TOP_LEFT, HudAnchor.TOP_CENTER, HudAnchor.TOP_RIGHT ->
                if (scaleY < 0f) bounds.top else bounds.bottom
            HudAnchor.MIDDLE_LEFT, HudAnchor.MIDDLE_CENTER, HudAnchor.MIDDLE_RIGHT -> bounds.bottom
            HudAnchor.BOTTOM_LEFT, HudAnchor.BOTTOM_CENTER, HudAnchor.BOTTOM_RIGHT ->
                if (scaleY < 0f) bounds.bottom else bounds.top
        }
        return x to y
    }
}

/** 활성 preview scene의 runtime layout을 선택용 guide snapshot으로 변환한다. */
object HudPreviewElementGuideCalculator {
    fun <S : KartState> calculate(scene: HudScene<S>): List<HudPreviewElementGuide> =
        scene.entries.mapIndexedNotNull { index, entry ->
            val element = entry.element ?: return@mapIndexedNotNull null
            val result = HudLayoutEngine.resolve(
                layout = HudLayoutSpec(
                    screenAnchor = element.screenAnchor,
                    elementAnchor = element.elementAnchor,
                    scaleX = element.scale.x,
                    scaleY = element.scale.y,
                    x = element.position.x,
                    y = element.position.y,
                    zIndex = element.zIndex,
                ),
                parentWidth = scene.width,
                parentHeight = scene.height,
                elementWidth = element.width,
                elementHeight = element.height,
            )
            HudPreviewElementGuide(
                elementId = entry.id,
                bounds = result.bounds,
                screenAnchorX = result.screenAnchorX,
                screenAnchorY = result.screenAnchorY,
                elementAnchorX = result.elementAnchorX,
                elementAnchorY = result.elementAnchorY,
                elementAnchor = element.elementAnchor,
                scaleX = element.scale.x,
                scaleY = element.scale.y,
                zIndex = result.zIndex,
                renderOrder = index,
            )
        }

    /** 겹친 요소 중 z-index가 높고, 같으면 나중에 렌더되는 요소를 선택한다. */
    fun hitTest(guides: List<HudPreviewElementGuide>, x: Float, y: Float): HudPreviewElementGuide? =
        guides.asSequence()
            .filter { it.contains(x, y) }
            .maxWithOrNull(compareBy<HudPreviewElementGuide> { it.zIndex }.thenBy { it.renderOrder })
}
