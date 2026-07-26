package io.github.oni0nfr1.dynamicrider.client.graphics.render.effect

import io.github.oni0nfr1.dynamicrider.client.graphics.render.batch
import io.github.oni0nfr1.dynamicrider.client.graphics.render.pipeline.DynRiderRenderPipelines
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import org.joml.Vector2f

/**
 * 텍스처 영역을 원점 [centerX], [centerY]에서 [startDirection]과 [endDirection] 사이의 각도만 보이게
 * 잘라 그린다.
 *
 * 중심 좌표는 지정한 source 영역의 좌상단을 원점으로 하는 텍스처 픽셀 좌표다. 방향벡터 역시 화면
 * 좌표계처럼 아래쪽이 양의 Y이며, [clockwise]가 참이면 시계 방향 영역을 남긴다.
 *
 * [fillAmount]는 0과 완전히 한 바퀴 찬 상태처럼 시작·종료 벡터만으로 구분할 수 없는 경계를 판별하는
 * 데 사용한다. 일반적인 부분 원호에서는 `0..1` 범위의 현재 진행률을 전달하면 된다.
 * [majorArc]는 두 방향 사이에서 180도를 초과하는 쪽을 남길지를 지정한다.
 */
fun GuiGraphics.blitArcClipped(
    texture: ResourceLocation,
    x: Int,
    y: Int,
    u: Float,
    v: Float,
    width: Int,
    height: Int,
    sourceWidth: Int,
    sourceHeight: Int,
    textureWidth: Int,
    textureHeight: Int,
    centerX: Float,
    centerY: Float,
    startDirection: Vector2f,
    endDirection: Vector2f,
    fillAmount: Float,
    clockwise: Boolean = true,
    majorArc: Boolean = false,
    color: Int = 0xFFFFFFFF.toInt(),
) {
    require(width > 0) { "width must be positive: $width" }
    require(height > 0) { "height must be positive: $height" }
    require(sourceWidth > 0) { "sourceWidth must be positive: $sourceWidth" }
    require(sourceHeight > 0) { "sourceHeight must be positive: $sourceHeight" }
    require(textureWidth > 0) { "textureWidth must be positive: $textureWidth" }
    require(textureHeight > 0) { "textureHeight must be positive: $textureHeight" }
    require(centerX.isFinite()) { "centerX must be finite: $centerX" }
    require(centerY.isFinite()) { "centerY must be finite: $centerY" }
    require(fillAmount.isFinite()) { "fillAmount must be finite: $fillAmount" }

    val start = normalizedDirection(startDirection, "startDirection")
    val end = normalizedDirection(endDirection, "endDirection")
    val progress = fillAmount.coerceIn(0f, 1f)
    if (progress <= 0f) return
    val sameBoundary = start.dot(end) >= 1f - DIRECTION_EPSILON
    val fullArc = progress >= 1f && sameBoundary

    val minU = u / textureWidth
    val maxU = (u + sourceWidth) / textureWidth
    val minV = v / textureHeight
    val maxV = (v + sourceHeight) / textureHeight
    val centerU = (u + centerX) / textureWidth
    val centerV = (v + centerY) / textureHeight
    val z = 0f

    batch(
        pipeline = DynRiderRenderPipelines.ARC_CLIP,
        samplers = mapOf("Sampler0" to texture),
        configurePass = {
            setUniform("ArcCenter", centerU, centerV)
            setUniform("ArcStartDirection", start.x, start.y)
            setUniform("ArcEndDirection", end.x, end.y)
            setUniform("ArcClockwise", if (clockwise) 1 else 0)
            setUniform("ArcMajor", if (majorArc) 1 else 0)
            setUniform("ArcFull", if (fullArc) 1 else 0)
        },
    ) { poseMatrix ->
        addVertex(poseMatrix, x.toFloat(), y.toFloat(), z)
            .setUv(minU, minV)
            .setColor(color)
        addVertex(poseMatrix, x.toFloat(), (y + height).toFloat(), z)
            .setUv(minU, maxV)
            .setColor(color)
        addVertex(poseMatrix, (x + width).toFloat(), (y + height).toFloat(), z)
            .setUv(maxU, maxV)
            .setColor(color)
        addVertex(poseMatrix, (x + width).toFloat(), y.toFloat(), z)
            .setUv(maxU, minV)
            .setColor(color)
    }
}

private const val DIRECTION_EPSILON = 0.00001f

private fun normalizedDirection(direction: Vector2f, name: String): Vector2f {
    require(direction.x.isFinite() && direction.y.isFinite()) {
        "$name must be finite: (${direction.x}, ${direction.y})"
    }
    require(direction.lengthSquared() > 0f) {
        "$name must not be a zero vector"
    }
    return Vector2f(direction).normalize()
}
