package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.layout.HudBounds
import io.github.oni0nfr1.dynamicrider.client.hud.layout.HudLayoutEngine
import kotlin.math.max
import kotlin.math.min

/** 부모 local 좌표를 preview scene 좌표로 옮기는 축 정렬 transform이다. */
data class HudPreviewParentTransform(
    val originX: Float,
    val originY: Float,
    val scaleX: Float,
    val scaleY: Float,
    val zIndex: Float,
) {
    fun toWorldX(localX: Float): Float = originX + localX * scaleX
    fun toWorldY(localY: Float): Float = originY + localY * scaleY

    fun toLocal(worldX: Float, worldY: Float): Pair<Float, Float>? {
        if (scaleX == 0f || scaleY == 0f) return null
        return (worldX - originX) / scaleX to (worldY - originY) / scaleY
    }

    companion object {
        val IDENTITY = HudPreviewParentTransform(0f, 0f, 1f, 1f, 0f)
    }
}

/** Preview 좌표계에서 경로 기반 선택과 layout 조작에 필요한 runtime 계산 결과다. */
data class HudPreviewElementGuide(
    val path: HudPath,
    val element: HudElement<*>,
    val bounds: HudBounds,
    val screenAnchorX: Float,
    val screenAnchorY: Float,
    val elementAnchorX: Float,
    val elementAnchorY: Float,
    val localScreenAnchorX: Float,
    val localScreenAnchorY: Float,
    val elementAnchor: HudAnchor,
    val scaleX: Float,
    val scaleY: Float,
    val zIndex: Float,
    val renderOrder: Int,
    val parentTransform: HudPreviewParentTransform,
) {
    val elementId: String get() = path.firstSegment
    val depth: Int get() = path.segments.size

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

/** 활성 preview scene의 runtime holder tree를 경로별 guide snapshot으로 변환한다. */
object HudPreviewElementGuideCalculator {
    fun calculate(root: ElementHolder): List<HudPreviewElementGuide> = buildList {
        var renderOrder = 0

        fun visit(holder: ElementHolder, parentPath: HudPath?, parentTransform: HudPreviewParentTransform) {
            holder.heldElements.forEach { entry ->
                val element = entry.element
                val path = parentPath?.child(entry.key) ?: HudPath.of(entry.key)
                val local = HudLayoutEngine.resolve(
                    layout = HudLayoutSpec(
                        screenAnchor = element.screenAnchor,
                        elementAnchor = element.elementAnchor,
                        scaleX = element.scale.x,
                        scaleY = element.scale.y,
                        x = element.position.x,
                        y = element.position.y,
                        zIndex = element.zIndex,
                    ),
                    parentWidth = holder.width,
                    parentHeight = holder.height,
                    elementWidth = element.width,
                    elementHeight = element.height,
                )
                val originX = parentTransform.toWorldX(local.renderX)
                val originY = parentTransform.toWorldY(local.renderY)
                val worldScaleX = parentTransform.scaleX * local.scaleX
                val worldScaleY = parentTransform.scaleY * local.scaleY
                val oppositeX = originX + element.width * worldScaleX
                val oppositeY = originY + element.height * worldScaleY
                val childTransform = HudPreviewParentTransform(
                    originX = originX,
                    originY = originY,
                    scaleX = worldScaleX,
                    scaleY = worldScaleY,
                    zIndex = parentTransform.zIndex + local.zIndex,
                )
                add(
                    HudPreviewElementGuide(
                        path = path,
                        element = element,
                        bounds = HudBounds(
                            min(originX, oppositeX),
                            min(originY, oppositeY),
                            max(originX, oppositeX),
                            max(originY, oppositeY),
                        ),
                        screenAnchorX = parentTransform.toWorldX(local.screenAnchorX),
                        screenAnchorY = parentTransform.toWorldY(local.screenAnchorY),
                        elementAnchorX = parentTransform.toWorldX(local.elementAnchorX),
                        elementAnchorY = parentTransform.toWorldY(local.elementAnchorY),
                        localScreenAnchorX = local.screenAnchorX,
                        localScreenAnchorY = local.screenAnchorY,
                        elementAnchor = element.elementAnchor,
                        scaleX = worldScaleX,
                        scaleY = worldScaleY,
                        zIndex = childTransform.zIndex,
                        renderOrder = renderOrder++,
                        parentTransform = parentTransform,
                    )
                )
                if (element is ElementHolder) visit(element, path, childTransform)
            }
        }

        visit(root, null, HudPreviewParentTransform.IDENTITY)
    }

    /** Pointer 아래 후보를 자손, 무관 요소, 현재 요소, 조상 순으로 비교한다. */
    fun hitTest(
        guides: List<HudPreviewElementGuide>,
        x: Float,
        y: Float,
        selectedPath: HudPath? = null,
    ): HudPreviewElementGuide? {
        val candidates = guides.filter { it.contains(x, y) }
        if (candidates.isEmpty()) return null
        return candidates.minWithOrNull { left, right ->
            compareCandidates(left, right, selectedPath)
        }
    }

    private fun compareCandidates(
        left: HudPreviewElementGuide,
        right: HudPreviewElementGuide,
        selectedPath: HudPath?,
    ): Int {
        if (selectedPath == null) {
            compareValues(left.depth, right.depth).takeIf { it != 0 }?.let { return it }
        } else {
            compareValues(relationRank(left.path, selectedPath), relationRank(right.path, selectedPath))
                .takeIf { it != 0 }
                ?.let { return it }
            compareValues(treeDistance(left.path, selectedPath), treeDistance(right.path, selectedPath))
                .takeIf { it != 0 }
                ?.let { return it }
        }
        compareValues(right.zIndex, left.zIndex).takeIf { it != 0 }?.let { return it }
        compareValues(right.renderOrder, left.renderOrder).takeIf { it != 0 }?.let { return it }
        return if (selectedPath == null) 0 else compareValues(right.depth, left.depth)
    }

    private fun relationRank(candidate: HudPath, selected: HudPath): Int = when {
        candidate.isDescendantOf(selected) -> 0
        candidate == selected -> 2
        selected.isDescendantOf(candidate) -> 3
        else -> 1
    }

    /** 가상 scene root를 포함한 두 요소 경로 사이의 edge 수를 반환한다. */
    fun treeDistance(left: HudPath, right: HudPath): Int {
        val commonDepth = left.segments.zip(right.segments).takeWhile { (a, b) -> a == b }.size
        return left.segments.size + right.segments.size - commonDepth * 2
    }

    private fun HudPath.isDescendantOf(parent: HudPath): Boolean =
        segments.size > parent.segments.size && segments.take(parent.segments.size) == parent.segments
}
