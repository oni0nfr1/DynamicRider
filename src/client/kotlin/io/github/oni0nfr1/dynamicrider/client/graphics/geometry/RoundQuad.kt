package io.github.oni0nfr1.dynamicrider.client.graphics.geometry

import org.joml.Vector2f
import kotlin.math.*

private const val EPS = 1e-6f
private const val TAU = (Math.PI.toFloat() * 2f)

private fun signedArea2(polygon: List<Vector2f>): Float {
    var sum = 0f
    for (i in polygon.indices) {
        val a = polygon[i]
        val b = polygon[(i + 1) % polygon.size]
        sum += a.x * b.y - b.x * a.y
    }
    return sum
}

private fun normalizeOrZero(v: Vector2f): Vector2f {
    val len2 = v.lengthSquared()
    if (len2 <= EPS) return Vector2f(0f, 0f)
    val invLen = 1f / sqrt(len2)
    v.mul(invLen)
    return v
}

/** (to - from) 단위벡터 반환 (새 Vector2f) */
private fun unitDir(from: Vector2f, to: Vector2f): Vector2f {
    val out = Vector2f()
    to.sub(from, out)          // out = to - from
    return normalizeOrZero(out)
}

private fun clamp(v: Float, lo: Float, hi: Float) = max(lo, min(hi, v))

/**
 * 한 코너에서 가능한 최대 필렛 반지름 상한을 계산해서, r이 너무 커서 접점이 변 밖으로 나가지 않게 막음.
 *
 * offset = r / tan(theta/2) <= min(edgeLenPrev, edgeLenNext)
 * => r <= minLen * tan(theta/2)
 */
private fun maxFilletRadiusAtCorner(prev: Vector2f, p: Vector2f, next: Vector2f): Float {
    val edgePrevLen = prev.distance(p)
    val edgeNextLen = next.distance(p)
    val minLen = min(edgePrevLen, edgeNextLen)

    val v1 = unitDir(p, prev)  // p -> prev
    val v2 = unitDir(p, next)  // p -> next
    val cosTheta = clamp(v1.dot(v2), -1f, 1f)
    val theta = acos(cosTheta)

    // 너무 평평하거나(≈π) 너무 뾰족하면(≈0) 필렛 계산이 의미 없어서 크게 제한
    if (theta <= 1e-3f || (Math.PI.toFloat() - theta) <= 1e-3f) return 0f

    val tanHalf = tan(theta * 0.5f)
    if (abs(tanHalf) <= EPS) return 0f

    return minLen * tanHalf
}

private data class CornerArc(
    val tangentPrev: Vector2f,
    val tangentNext: Vector2f,
    val center: Vector2f,
    val anglePrev: Float,
    val angleNext: Float,
)

/**
 * (볼록) 폴리곤의 라운드 처리된 외곽선 점 리스트 생성
 * - polygon은 경계 순서대로(CW 또는 CCW) 들어와야 함
 * - radius는 내부에서 안전하게 클램프됨
 */
fun buildRoundedOutlinePoints(
    polygon: List<Vector2f>,
    requestedRadius: Float,
    arcSegments: Int,
): List<Vector2f> {
    require(polygon.size >= 3)

    var safeRadius = requestedRadius
    for (i in polygon.indices) {
        val prev = polygon[(i - 1 + polygon.size) % polygon.size]
        val p = polygon[i]
        val next = polygon[(i + 1) % polygon.size]
        val maxR = maxFilletRadiusAtCorner(prev, p, next)
        safeRadius = min(safeRadius, maxR)
    }
    if (safeRadius <= 0.01f) {
        // 라운드가 사실상 불가능하면 그냥 원본 리턴
        return polygon.map { Vector2f(it) }
    }

    val clockwise = signedArea2(polygon) < 0f

    val cornerArcs = ArrayList<CornerArc>(polygon.size)

    for (i in polygon.indices) {
        val prev = polygon[(i - 1 + polygon.size) % polygon.size]
        val p = polygon[i]
        val next = polygon[(i + 1) % polygon.size]

        val vPrev = unitDir(p, prev) // p -> prev
        val vNext = unitDir(p, next) // p -> next

        val cosTheta = clamp(vPrev.dot(vNext), -1f, 1f)
        val theta = acos(cosTheta)
        val half = theta * 0.5f

        val tanHalf = tan(half)
        val sinHalf = sin(half)
        if (abs(tanHalf) <= EPS || abs(sinHalf) <= EPS) {
            // 비정상 코너면 그냥 점으로 대체
            cornerArcs += CornerArc(Vector2f(p), Vector2f(p), Vector2f(p), 0f, 0f)
            continue
        }

        val offset = safeRadius / tanHalf
        val distToCenter = safeRadius / sinHalf

        val tangentPrev = Vector2f(vPrev).mul(offset).add(p) // p + vPrev*offset
        val tangentNext = Vector2f(vNext).mul(offset).add(p) // p + vNext*offset

        // 이등분선 방향 (vPrev + vNext)
        val bisector = Vector2f(vPrev).add(vNext)
        normalizeOrZero(bisector)

        // 중심
        val center = Vector2f(bisector).mul(distToCenter).add(p)

        val anglePrev = atan2(tangentPrev.y - center.y, tangentPrev.x - center.x)
        val angleNext = atan2(tangentNext.y - center.y, tangentNext.x - center.x)

        cornerArcs += CornerArc(tangentPrev, tangentNext, center, anglePrev, angleNext)
    }

    val outline = ArrayList<Vector2f>(polygon.size * (arcSegments + 2))

    fun addArcPoints(arc: CornerArc, includeFirst: Boolean) {
        val a0 = arc.anglePrev
        val a1 = arc.angleNext
        var delta = a1 - a0

        // 폴리곤 진행 방향에 맞게 각도 진행 방향을 강제
        if (clockwise) {
            // 시계방향으로 돌 때: 각도는 감소하는 방향으로 가는 게 일관됨
            if (delta > 0f) delta -= TAU
        } else {
            // 반시계방향: 각도는 증가
            if (delta < 0f) delta += TAU
        }

        val startK = if (includeFirst) 0 else 1
        for (k in startK..arcSegments) {
            val t = k / arcSegments.toFloat()
            val a = a0 + delta * t
            val px = arc.center.x + cos(a) * safeRadius
            val py = arc.center.y + sin(a) * safeRadius
            outline += Vector2f(px, py)
        }
    }

    addArcPoints(cornerArcs[0], includeFirst = true)
    for (i in 1 until cornerArcs.size) addArcPoints(cornerArcs[i], includeFirst = false)

    return outline
}
