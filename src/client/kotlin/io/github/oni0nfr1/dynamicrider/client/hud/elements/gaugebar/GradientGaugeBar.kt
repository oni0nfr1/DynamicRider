package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar

import com.mojang.math.Axis
import io.github.oni0nfr1.dynamicrider.client.graphics.util.drawScaledText
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge.LinearExtrapolator
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudColor
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudElementInfo
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudLayout
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudRange
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HexColorSerdes
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics

class GradientGaugeBar(
    spec: Spec,
    context: HudSceneContext<NitroKartState>,
    parent: ElementHolder,
) : HudElementImpl<NitroKartState>(spec.layout, context, parent),
    GaugeBar by LinearExtrapolator(context.kartState) {
    companion object {
        val client: Minecraft by lazy { Minecraft.getInstance() }
        val fontManager: Font = client.font

        const val N2O_LABEL_TEXT = "N2O"

        val N2O_LABEL_WIDTH: Int by lazy {
            fontManager.width(N2O_LABEL_TEXT)
        }
    }

    var thickness: Int = spec.thickness
    var barWidth: Int = spec.width
    var padding: Int = spec.padding
    var boxColor: Int = spec.boxColor
    var gaugeAlpha: Int = spec.gaugeAlpha
    var gradientStops: List<ColorStop> = spec.gradientStops

    override val width: Int
        get() = barWidth + padding * 2
    override val height: Int
        get() = thickness + padding * 2

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        updateGauge(deltaTracker.realtimeDeltaTicks)

        guiGraphics.fill(0, 0, width, height, boxColor)

        val displayGauge = nitroGauge.coerceIn(0f, 1f)
        val filledWidth = (barWidth * displayGauge).toInt().coerceIn(0, barWidth)

        val gaugeStops = gradientStops.map { it.offset to withAlpha(it.color, gaugeAlpha) }

        val labelScale = thickness / fontManager.lineHeight.toFloat()

        val pose = guiGraphics.pose()
        pose.pushPose()
        pose.translate(-N2O_LABEL_WIDTH * labelScale, 0f, 0f)
        guiGraphics.drawScaledText(
            -padding,
            padding,
            labelScale,
            N2O_LABEL_TEXT,
            0x80FFFFFF.toInt(),
        )
        pose.popPose()

        drawMultiStopGradientGauge(
            guiGraphics = guiGraphics,
            barLeftX = padding,
            barTopY = padding,
            barWidthPx = barWidth,
            barHeightPx = thickness,
            filledWidthPx = filledWidth,
            stops = gaugeStops,
        )
    }

    private fun drawMultiStopGradientGauge(
        guiGraphics: GuiGraphics,
        barLeftX: Int,
        barTopY: Int,
        barWidthPx: Int,
        barHeightPx: Int,
        filledWidthPx: Int,
        stops: List<Pair<Int, Int>>,
    ) {
        if (barWidthPx <= 0 || barHeightPx <= 0) return
        if (filledWidthPx <= 0) return
        if (stops.size < 2) return

        val barRightXExclusive = barLeftX + barWidthPx
        val filledRightXExclusive = (barLeftX + filledWidthPx).coerceAtMost(barRightXExclusive)

        val sorted = stops
            .map { (offset, color) -> offset.coerceAtLeast(0) to color }
            .sortedBy { it.first }

        val normalized = if (sorted.first().first != 0) {
            listOf(0 to sorted.first().second) + sorted
        } else {
            sorted
        }

        for (i in 0 until normalized.size - 1) {
            val (offA, colorA) = normalized[i]
            val (offB, colorB) = normalized[i + 1]

            val xA = (barLeftX + offA).coerceIn(barLeftX, barRightXExclusive)
            val xB = (barLeftX + offB).coerceIn(barLeftX, barRightXExclusive)

            val segStart = xA.coerceAtMost(filledRightXExclusive)
            val segEnd = xB.coerceAtMost(filledRightXExclusive)
            if (segEnd <= segStart) continue

            fillHorizontalGradientByRotatingVertical(
                guiGraphics = guiGraphics,
                x0 = segStart,
                y0 = barTopY,
                widthPx = segEnd - segStart,
                heightPx = barHeightPx,
                leftColor = colorA,
                rightColor = colorB,
            )
        }

        val lastOff = normalized.last().first
        val lastColor = normalized.last().second
        val lastStart = (barLeftX + lastOff).coerceIn(barLeftX, barRightXExclusive)
        val solidStart = lastStart.coerceAtMost(filledRightXExclusive)
        if (filledRightXExclusive > solidStart) {
            guiGraphics.fill(solidStart, barTopY, filledRightXExclusive, barTopY + barHeightPx, lastColor)
        }
    }

    private fun fillHorizontalGradientByRotatingVertical(
        guiGraphics: GuiGraphics,
        x0: Int,
        y0: Int,
        widthPx: Int,
        heightPx: Int,
        leftColor: Int,
        rightColor: Int,
    ) {
        if (widthPx <= 0 || heightPx <= 0) return

        val pose = guiGraphics.pose()
        pose.pushPose()

        pose.translate(x0.toDouble(), (y0 + heightPx).toDouble(), 0.0)
        pose.mulPose(Axis.ZP.rotationDegrees(-90f))

        guiGraphics.fillGradient(0, 0, heightPx, widthPx, leftColor, rightColor)

        pose.popPose()
    }

    private fun withAlpha(argb: Int, alpha0to255: Int): Int {
        val a = alpha0to255.coerceIn(0, 255)
        return (a shl 24) or (argb and 0x00FFFFFF)
    }

    @Serializable
    @SerialName("GRADIENT_GAUGE_BAR")
    @HudElementInfo(category = "gauge")
    data class Spec(
        @HudLayout
        override val layout: HudLayoutSpec = HudLayoutSpec(),
        @HudRange(min = 1.0, max = 64.0, step = 1.0)
        val thickness: Int = 8,
        @HudRange(min = 1.0, max = 2_048.0, step = 1.0)
        val width: Int = 120,
        @HudRange(min = 0.0, max = 64.0, step = 1.0)
        val padding: Int = 2,
        @HudColor
        @Serializable(with = HexColorSerdes::class)
        val boxColor: Int = 0x80000000.toInt(),
        @HudRange(min = 0.0, max = 255.0, step = 1.0)
        val gaugeAlpha: Int = 0xFF,
        val gradientStops: List<ColorStop> = ColorStop.default(),
    ) : HudElementSpec<GradientGaugeBar, NitroKartState> {
        override fun requiredStateClass(): Class<out NitroKartState> = NitroKartState::class.java

        override fun create(context: HudSceneContext<NitroKartState>, parent: ElementHolder) =
            GradientGaugeBar(this, context, parent)
    }

    @Serializable
    data class ColorStop(
        val offset: Int,
        @HudColor
        @Serializable(with = HexColorSerdes::class)
        val color: Int,
    ) {
        companion object {
            fun default(): List<ColorStop> = listOf(
                ColorStop(offset = 0, color = 0xFFFFFFFF.toInt()),
                ColorStop(offset = 30, color = 0xFFFFE8A1.toInt()),
                ColorStop(offset = 60, color = 0xFFFFC040.toInt()),
                ColorStop(offset = 90, color = 0xFFFF5E18.toInt()),
                ColorStop(offset = 120, color = 0xFFFF0000.toInt()),
            )

        }
    }
}
