@file:Suppress("UNUSED")
package io.github.oni0nfr1.dynamicrider.client.graphics.mesh

import com.mojang.blaze3d.vertex.BufferBuilder
import io.github.oni0nfr1.dynamicrider.client.util.colorFromARGB
import org.joml.Matrix4f
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

fun BufferBuilder.arcRingHaloVertex(
    poseMatrix: Matrix4f,
    centerX: Float, centerY: Float, zIndex: Float,
    innerRadius: Float, outerRadius: Float,
    startRad: Float, endRad: Float,
    r: Int, g: Int, b: Int,
    innerAlpha: Int, outerAlpha: Int,
    segments: Int,
) {
    val innerArgb = colorFromARGB(innerAlpha, r, g, b)
    val outerArgb = colorFromARGB(outerAlpha, r, g, b)

    val radLo = min(startRad, endRad)
    val radHi = max(startRad, endRad)
    val deltaRad = (radHi - radLo) / segments.toFloat()

    for (i in 0 until segments) {
        val rad0 = radLo + deltaRad * i
        val rad1 = radLo + deltaRad * (i + 1)

        val cos0 = cos(rad0); val sin0 = sin(rad0)
        val cos1 = cos(rad1); val sin1 = sin(rad1)

        val x0in = centerX + innerRadius * cos0
        val y0in = centerY + innerRadius * sin0
        val x1in = centerX + innerRadius * cos1
        val y1in = centerY + innerRadius * sin1

        val x0out = centerX + outerRadius * cos0
        val y0out = centerY + outerRadius * sin0
        val x1out = centerX + outerRadius * cos1
        val y1out = centerY + outerRadius * sin1

        this.addVertex(poseMatrix, x0in,  y0in,  zIndex).setColor(innerArgb)
        this.addVertex(poseMatrix, x1in,  y1in,  zIndex).setColor(innerArgb)
        this.addVertex(poseMatrix, x1out, y1out, zIndex).setColor(outerArgb)
        this.addVertex(poseMatrix, x0out, y0out, zIndex).setColor(outerArgb)
    }
}

fun BufferBuilder.arcRingHaloVertexFadeY(
    poseMatrix: Matrix4f,
    centerX: Float, centerY: Float, zIndex: Float,
    innerRadius: Float, outerRadius: Float,
    startRad: Float, endRad: Float,
    r: Int, g: Int, b: Int,
    innerAlpha: Int, outerAlpha: Int,
    fadeTopY: Float, fadeBottomY: Float,
    segments: Int,
) {
    val radLo = min(startRad, endRad)
    val radHi = max(startRad, endRad)
    val deltaRad = (radHi - radLo) / segments.toFloat()

    for (i in 0 until segments) {
        val rad0 = radLo + deltaRad * i
        val rad1 = radLo + deltaRad * (i + 1)

        val cos0 = cos(rad0); val sin0 = sin(rad0)
        val cos1 = cos(rad1); val sin1 = sin(rad1)

        val x0in = centerX + innerRadius * cos0
        val y0in = centerY + innerRadius * sin0
        val x1in = centerX + innerRadius * cos1
        val y1in = centerY + innerRadius * sin1

        val x0out = centerX + outerRadius * cos0
        val y0out = centerY + outerRadius * sin0
        val x1out = centerX + outerRadius * cos1
        val y1out = centerY + outerRadius * sin1

        val a0in = (fade01(y0in, fadeTopY, fadeBottomY) * innerAlpha).toInt().coerceIn(0, 255)
        val a1in = (fade01(y1in, fadeTopY, fadeBottomY) * innerAlpha).toInt().coerceIn(0, 255)
        val a0out = (fade01(y0out, fadeTopY, fadeBottomY) * outerAlpha).toInt().coerceIn(0, 255)
        val a1out = (fade01(y1out, fadeTopY, fadeBottomY) * outerAlpha).toInt().coerceIn(0, 255)

        this.addVertex(poseMatrix, x0in,  y0in,  zIndex)
            .setColor(colorFromARGB(a0in, r, g, b))
        this.addVertex(poseMatrix, x1in,  y1in,  zIndex)
            .setColor(colorFromARGB(a1in, r, g, b))
        this.addVertex(poseMatrix, x1out, y1out, zIndex)
            .setColor(colorFromARGB(a1out, r, g, b))
        this.addVertex(poseMatrix, x0out, y0out, zIndex)
            .setColor(colorFromARGB(a0out, r, g, b))
    }
}

fun BufferBuilder.arcRingHaloVertexFadeRadAndY(
    poseMatrix: Matrix4f,
    centerX: Float, centerY: Float, zIndex: Float,
    innerRadius: Float, outerRadius: Float,
    startRad: Float, fadeStartRad: Float, endRad: Float,
    r: Int, g: Int, b: Int,
    innerAlpha: Int, outerAlpha: Int,
    fadeTopY: Float, fadeBottomY: Float,
    segments: Int,
) {
    val radLo = min(startRad, endRad)
    val radHi = max(startRad, endRad)
    val deltaRad = (radHi - radLo) / segments.toFloat()

    val arcLen = abs(endRad - startRad)
    val fadeLen = abs(fadeStartRad - endRad).coerceIn(0f, arcLen)

    for (i in 0 until segments) {
        val rad0 = radLo + deltaRad * i
        val rad1 = radLo + deltaRad * (i + 1)

        val fRad0 = if (fadeLen <= 1e-6f) 1f else fade01(abs(rad0 - endRad), 0f, fadeLen)
        val fRad1 = if (fadeLen <= 1e-6f) 1f else fade01(abs(rad1 - endRad), 0f, fadeLen)

        val cos0 = cos(rad0); val sin0 = sin(rad0)
        val cos1 = cos(rad1); val sin1 = sin(rad1)

        val x0in = centerX + innerRadius * cos0
        val y0in = centerY + innerRadius * sin0
        val x1in = centerX + innerRadius * cos1
        val y1in = centerY + innerRadius * sin1

        val x0out = centerX + outerRadius * cos0
        val y0out = centerY + outerRadius * sin0
        val x1out = centerX + outerRadius * cos1
        val y1out = centerY + outerRadius * sin1

        val fY0in  = fade01(y0in,  fadeTopY, fadeBottomY)
        val fY1in  = fade01(y1in,  fadeTopY, fadeBottomY)
        val fY0out = fade01(y0out, fadeTopY, fadeBottomY)
        val fY1out = fade01(y1out, fadeTopY, fadeBottomY)

        val a0in  = (innerAlpha * fRad0 * fY0in).toInt().coerceIn(0, 255)
        val a1in  = (innerAlpha * fRad1 * fY1in).toInt().coerceIn(0, 255)
        val a0out = (outerAlpha * fRad0 * fY0out).toInt().coerceIn(0, 255)
        val a1out = (outerAlpha * fRad1 * fY1out).toInt().coerceIn(0, 255)

        addVertex(poseMatrix, x0in,  y0in,  zIndex).setColor(colorFromARGB(a0in, r, g, b))
        addVertex(poseMatrix, x1in,  y1in,  zIndex).setColor(colorFromARGB(a1in, r, g, b))
        addVertex(poseMatrix, x1out, y1out, zIndex).setColor(colorFromARGB(a1out, r, g, b))
        addVertex(poseMatrix, x0out, y0out, zIndex).setColor(colorFromARGB(a0out, r, g, b))
    }
}

fun BufferBuilder.arcRingVertex(
    poseMatrix: Matrix4f,
    centerX: Float, centerY: Float, zIndex: Float,
    innerRadius: Float, outerRadius: Float,
    startRad: Float, endRad: Float,
    r: Int, g: Int, b: Int, a: Int,
    segments: Int,
) = (this::arcRingHaloVertex)(
    poseMatrix,
    centerX, centerY, zIndex,
    innerRadius, outerRadius,
    startRad, endRad,
    r, g, b,
    a, a,
    segments,
)

fun BufferBuilder.arcRingVertexFadeY(
    poseMatrix: Matrix4f,
    centerX: Float, centerY: Float, zIndex: Float,
    innerRadius: Float, outerRadius: Float,
    startRad: Float, endRad: Float,
    r: Int, g: Int, b: Int, a: Int,
    fadeTopY: Float, fadeBottomY: Float,
    segments: Int,
) = (this::arcRingHaloVertexFadeY)(
    poseMatrix,
    centerX, centerY, zIndex,
    innerRadius, outerRadius,
    startRad, endRad,
    r, g, b,
    a, a,
    fadeTopY, fadeBottomY,
    segments,
)

fun BufferBuilder.arcRingVertexFadeRadAndY(
    poseMatrix: Matrix4f,
    centerX: Float, centerY: Float, zIndex: Float,
    innerRadius: Float, fadeStartRad: Float, outerRadius: Float,
    startRad: Float, endRad: Float,
    r: Int, g: Int, b: Int, a: Int,
    fadeTopY: Float, fadeBottomY: Float,
    segments: Int,
) = (this::arcRingHaloVertexFadeRadAndY)(
    poseMatrix,
    centerX, centerY, zIndex,
    innerRadius, outerRadius,
    startRad, fadeStartRad, endRad,
    r, g, b,
    a, a,
    fadeTopY, fadeBottomY,
    segments,
)

fun fadeFactor(value: Float, fadeEnd: Float, fadeStart: Float): Float {
    if (fadeEnd <= fadeStart) return 1f
    val t = (value - fadeStart) / (fadeEnd - fadeStart)
    val clamped = t.coerceIn(0f, 1f)
    return clamped * clamped * (3f - 2f * clamped)
}

fun fade01(value: Float, start: Float, end: Float): Float =
    fadeFactor(value, end, start)

fun fade10(value: Float, start: Float, end: Float): Float =
    1f - fadeFactor(value, end, start)
