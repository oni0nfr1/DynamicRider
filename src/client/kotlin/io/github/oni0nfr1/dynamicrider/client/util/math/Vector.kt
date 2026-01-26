package io.github.oni0nfr1.dynamicrider.client.util.math

import org.joml.Vector2f
import kotlin.math.atan2
import kotlin.math.sqrt

private const val EPSILON = 1e-6f

fun directionRad(v: Vector2f): Float = atan2(v.y, v.x)

fun Vector2f.normalizedOrZero(): Vector2f {
    val len2 = this.lengthSquared()
    return if (len2 <= EPSILON) Vector2f(0f, 0f) else this.mul((1f / sqrt(len2)))
}

/** fromVec -> this 방향 단위벡터 */
infix fun Vector2f.from(fromVec: Vector2f): Vector2f {
    val result = Vector2f()
    this.sub(fromVec, result)
    return result.normalizedOrZero()
}

/** 각 이등분 방향(단위벡터). v1 == -v2면 fallback */
fun bisector(v1: Vector2f, v2: Vector2f): Vector2f {
    val sum = Vector2f(v1).add(v2)
    if (sum.lengthSquared() <= EPSILON) {
        // fallback: v1을 90도 회전 (어차피 이 케이스는 입력이 이상한 상황이 많음)
        return Vector2f(-v1.y, v1.x).normalizedOrZero()
    }
    return sum.normalizedOrZero()
}