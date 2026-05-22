package io.github.oni0nfr1.dynamicrider.client.graphics.util

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import io.github.oni0nfr1.dynamicrider.client.graphics.mesh.arcRingHaloVertexFadeRadAndY
import io.github.oni0nfr1.dynamicrider.client.graphics.mesh.arcRingHaloVertexFadeY
import io.github.oni0nfr1.dynamicrider.client.graphics.mesh.arcRingVertex
import io.github.oni0nfr1.dynamicrider.client.graphics.mesh.arcRingVertexFadeRadAndY
import io.github.oni0nfr1.dynamicrider.client.graphics.render.DynRiderRenderTypes
import io.github.oni0nfr1.dynamicrider.client.graphics.render.batch
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix4f

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