package io.github.oni0nfr1.dynamicrider.client.graphics

import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import io.github.oni0nfr1.dynamicrider.client.config.DynRiderConfig
import io.github.oni0nfr1.dynamicrider.client.graphics.geometry.buildRoundedOutlinePoints
import io.github.oni0nfr1.dynamicrider.client.graphics.mesh.arcRingHaloVertexFadeRadAndY
import io.github.oni0nfr1.dynamicrider.client.graphics.mesh.arcRingHaloVertexFadeY
import io.github.oni0nfr1.dynamicrider.client.graphics.mesh.arcRingVertex
import io.github.oni0nfr1.dynamicrider.client.graphics.mesh.arcRingVertexFadeRadAndY
import io.github.oni0nfr1.dynamicrider.client.graphics.render.DynRiderRenderTypes
import io.github.oni0nfr1.dynamicrider.client.util.colorFromARGB
import io.github.oni0nfr1.dynamicrider.client.util.dsegText
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.network.chat.Component
import org.joml.Matrix4f
import org.joml.Vector2f

/*
    렌더 관련 함수들이 들어갑니다.
    좀더 다양한 디자인 구현을 위해 커스텀 draw call이 추가될 수도 있습니다.
*/

fun GuiGraphics.textWithDynriderFont(
    x: Int,
    y: Int,
    argbColor: Int,
    text: String,
    shadow: Boolean = false
) {
    val client = Minecraft.getInstance()
    val font = DynRiderConfig.hudFont.style
    val component = Component.literal(text).setStyle(font)
    this.drawString(client.font, component, x, y, argbColor, shadow)
}

fun GuiGraphics.drawSpeed7Seg(
    xCenter: Float,
    yTop: Float,
    speed: Int,
    onArgb: Int,
    offArgb: Int = 0x60000000,
    unitText: String,
    unitPadding: Float = 4f,
    shadow: Boolean = false,
) {
    val font = Minecraft.getInstance().font

    val base = dsegText("888")
    val boxWidth = font.width(base).toFloat()
    val rightEdge = xCenter + boxWidth * 0.5f
    val leftEdge = rightEdge - boxWidth

    drawString(font, base, leftEdge.toInt(), yTop.toInt(), offArgb, shadow)

    val digits = if (speed in 0..999) speed.toString() else speed.toString()
    val comp = dsegText(digits)
    val digitsWidth = font.width(comp).toFloat()
    val xLeft = rightEdge - digitsWidth

    drawString(font, comp, xLeft.toInt(), yTop.toInt(), onArgb, shadow)

    val unitX = rightEdge + unitPadding
    drawString(font, Component.literal(unitText), unitX.toInt(), yTop.toInt(), onArgb, false)
}


fun GuiGraphics.drawArcRing(
    centerX: Float,
    centerY: Float,
    radius: Float,
    thickness: Float,
    startRad: Float,
    endRad: Float,
    argb: Int,
    segments: Int = 64,
) {
    val a = (argb ushr 24) and 0xFF
    val r = (argb ushr 16) and 0xFF
    val g = (argb ushr 8) and 0xFF
    val b = argb and 0xFF

    val inner = radius - thickness * 0.5f
    val outer = radius + thickness * 0.5f

    this.batch(
        VertexFormat.Mode.QUADS,
        DefaultVertexFormat.POSITION_COLOR,
        DynRiderRenderTypes.ARC_CORE
    ) { poseMatrix: Matrix4f ->
        arcRingVertex(
            poseMatrix,
            centerX, centerY, zIndex = 0f,
            innerRadius = inner, outerRadius = outer,
            startRad = startRad, endRad = endRad,
            r = r, g = g, b = b, a = a,
            segments,
        )
    }
}

fun GuiGraphics.drawJiuEngineArcGlow(
    centerX: Float,
    centerY: Float,
    radius: Float,
    coreThickness: Float,
    haloThicknessOuter: Float,
    haloThicknessInner: Float,
    startRad: Float,
    fadeStartRad: Float,
    endRad: Float,
    argb: Int,
    glowLayers: Int,
    fadeTopY: Float,
    fadeBottomY: Float,
    segments: Int = 48,
) {
    val a = (argb ushr 24) and 0xFF
    val r = (argb ushr 16) and 0xFF
    val g = (argb ushr 8) and 0xFF
    val b = argb and 0xFF

    val coreInner = radius - coreThickness * 0.5f
    val coreOuter = radius + coreThickness * 0.5f

    for (i in 0 until glowLayers) {
        val t0 = i / glowLayers.toFloat()
        val t1 = (i + 1) / glowLayers.toFloat()

        val out0 = coreOuter + haloThicknessOuter * t0
        val out1 = coreOuter + haloThicknessOuter * t1

        val in0 = coreInner - haloThicknessInner * t1
        val in1 = coreInner - haloThicknessInner * t0

        val alphaInner = (a * (0.35f * (1f - t0))).toInt().coerceIn(0, 255)
        val alphaOuter = 0

        this.batch(
            VertexFormat.Mode.QUADS,
            DefaultVertexFormat.POSITION_COLOR,
            DynRiderRenderTypes.ARC_HALO,
        ) { poseMatrix: Matrix4f ->
            arcRingHaloVertexFadeRadAndY(
                poseMatrix,
                centerX, centerY, zIndex = 0f,
                innerRadius = out0, outerRadius = out1,
                startRad, fadeStartRad, endRad,
                r = r, g = g, b = b,
                innerAlpha = alphaInner,
                outerAlpha = alphaOuter,
                fadeTopY, fadeBottomY,
                segments = segments,
            )
            arcRingHaloVertexFadeRadAndY(
                poseMatrix,
                centerX, centerY, zIndex = 0f,
                innerRadius = in0, outerRadius = in1,
                startRad, fadeStartRad, endRad,
                r = r, g = g, b = b,
                innerAlpha = alphaOuter,
                outerAlpha = alphaInner,
                fadeTopY, fadeBottomY,
                segments = segments,
            )
        }
    }

    this.batch(
        VertexFormat.Mode.QUADS,
        DefaultVertexFormat.POSITION_COLOR,
        DynRiderRenderTypes.ARC_CORE,
    ) { poseMatrix: Matrix4f ->
        arcRingVertexFadeRadAndY(
            poseMatrix,
            centerX, centerY, 0f,
            innerRadius = coreInner, outerRadius = coreOuter,
            startRad = startRad, fadeStartRad = fadeStartRad, endRad = endRad,
            r = r, g = g, b = b, a = a,
            fadeTopY = fadeTopY, fadeBottomY = fadeBottomY,
            segments = segments,
        )
    }
}

fun GuiGraphics.drawJiuEngineArcGlowEmpty(
    centerX: Float,
    centerY: Float,
    radius: Float,
    coreThickness: Float,
    haloThicknessOuter: Float,
    haloThicknessInner: Float,
    startRad: Float,
    endRad: Float,
    argb: Int,
    glowLayers: Int,
    fadeTopY: Float,
    fadeBottomY: Float,
    segments: Int = 48,
) {
    val a = (argb ushr 24) and 0xFF
    val r = (argb ushr 16) and 0xFF
    val g = (argb ushr 8) and 0xFF
    val b = argb and 0xFF

    val coreInner = radius - coreThickness * 0.5f
    val coreOuter = radius + coreThickness * 0.5f

    this.batch(
        VertexFormat.Mode.QUADS,
        DefaultVertexFormat.POSITION_COLOR,
        DynRiderRenderTypes.ARC_CORE,
    ) { poseMatrix: Matrix4f ->
        for (i in 0 until glowLayers) {
            val t0 = i / glowLayers.toFloat()
            val t1 = (i + 1) / glowLayers.toFloat()

            val out0 = coreOuter + haloThicknessOuter * t0
            val out1 = coreOuter + haloThicknessOuter * t1

            val in0 = coreInner - haloThicknessInner * t1
            val in1 = coreInner - haloThicknessInner * t0

            val alphaInner = (a * (0.35f * (1f - t0))).toInt().coerceIn(0, 255)
            val alphaOuter = 0

            arcRingHaloVertexFadeY(
                poseMatrix,
                centerX, centerY, zIndex = 0f,
                innerRadius = out0, outerRadius = out1,
                startRad = startRad, endRad = endRad,
                r = r, g = g, b = b,
                innerAlpha = alphaInner,
                outerAlpha = alphaOuter,
                fadeTopY, fadeBottomY,
                segments = segments,
            )
            arcRingHaloVertexFadeY(
                poseMatrix,
                centerX, centerY, zIndex = 0f,
                innerRadius = in0, outerRadius = in1,
                startRad = startRad, endRad = endRad,
                r = r, g = g, b = b,
                innerAlpha = alphaOuter,
                outerAlpha = alphaInner,
                fadeTopY, fadeBottomY,
                segments = segments,
            )
        }
    }
}

fun GuiGraphics.fillRoundedTrapezoid(
    x: Float,
    y: Float,
    topWidth: Float,
    bottomWidth: Float,
    height: Float,
    cornerRadius: Float,
    argb: Int,
    arcSegments: Int = 6,
) {
    val dx = (topWidth - bottomWidth) * 0.5f

    val topLeft = Vector2f(x, y)
    val topRight = Vector2f(x + topWidth, y)
    val bottomRight = Vector2f(x + dx + bottomWidth, y + height)
    val bottomLeft = Vector2f(x + dx, y + height)

    val polygon = listOf(topLeft, topRight, bottomRight, bottomLeft)
    val outline = buildRoundedOutlinePoints(polygon, cornerRadius, arcSegments)
    if (outline.size < 3) return

    var centerX = 0f
    var centerY = 0f
    for (p in outline) { centerX += p.x; centerY += p.y }
    centerX /= outline.size.toFloat()
    centerY /= outline.size.toFloat()

    val alpha = (argb ushr 24) and 0xFF
    val red   = (argb ushr 16) and 0xFF
    val green = (argb ushr 8) and 0xFF
    val blue  = (argb) and 0xFF
    val color = colorFromARGB(alpha, red, green, blue)

    this.batch(
        VertexFormat.Mode.TRIANGLES,
        DefaultVertexFormat.POSITION_COLOR,
        DynRiderRenderTypes.ARC_CORE_TRI
    ) { poseMatrix: Matrix4f ->
        val n = outline.size
        for (i in 0 until n) {
            val p0 = outline[i]
            val p1 = outline[(i + 1) % n]

            addVertex(poseMatrix, centerX, centerY, 0f)
                .setColor(color)
            addVertex(poseMatrix, p0.x, p0.y, 0f)
                .setColor(color)
            addVertex(poseMatrix, p1.x, p1.y, 0f)
                .setColor(color)
        }
    }
}

inline fun GuiGraphics.batch(
    mode: VertexFormat.Mode,
    vertexFormat: VertexFormat,
    renderType: RenderType,
    builderFunction: BufferBuilder.(Matrix4f) -> Unit,
) {
    val poseMatrix = this.pose().last().pose()
    val tesselator = Tesselator.getInstance()
    val builder = tesselator.begin(mode, vertexFormat)

    builder.builderFunction(poseMatrix)

    val meshData = builder.build()
    renderType.draw(meshData)
}
