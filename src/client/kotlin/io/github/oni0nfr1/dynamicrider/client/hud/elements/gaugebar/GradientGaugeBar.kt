package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar

import com.mojang.math.Axis
import io.github.oni0nfr1.dynamicrider.client.graphics.drawScaledText
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.GaugeBar as GaugeBarElement
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import kotlin.math.exp

class GradientGaugeBar(
    spec: GradientGaugeBarSpec,
    kart: KartRef.Specific<NitroEngine>,
) : HudElementImpl<NitroEngine>(spec.layout, kart), GaugeBarElement {
    companion object {
        val client: Minecraft by lazy { Minecraft.getInstance() }
        val fontManager: Font = client.font

        const val N2O_LABEL_TEXT = "N2O"

        val N2O_LABEL_WIDTH: Int by lazy {
            fontManager.width(N2O_LABEL_TEXT)
        }
    }

    var thickness: Int = spec.thickness
    var width: Int = spec.width
    var padding: Int = spec.padding
    var boxColor: Int = spec.boxColor
    var gaugeAlpha: Int = spec.gaugeAlpha
    var targetGaugeAlpha: Int = spec.targetGaugeAlpha
    var smoothing: Double = spec.smoothing
    var gradientStops: List<GradientGaugeBarStopSpec> = spec.gradientStops

    override val gauge: Double
        get() = kart.accessEngine { engine ->
            engine.tachometer?.gauge
        } ?: 0.0
    private var displayGauge: Double = 0.0

    override fun resolveSize() {
        setSize(width + padding * 2, thickness + padding * 2)
    }

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        val targetGauge = gauge.coerceIn(0.0, 1.0)
        val dtTicks = deltaTracker.realtimeDeltaTicks.toDouble()
        val follow = 1.0 - exp(-smoothing * dtTicks)
        displayGauge += (targetGauge - displayGauge) * follow
        displayGauge = displayGauge.coerceIn(0.0, 1.0)

        guiGraphics.fill(0, 0, size.x, size.y, boxColor)

        val targetFilledWidth = (width * targetGauge).toInt().coerceIn(0, width)
        val filledWidth = (width * displayGauge).toInt().coerceIn(0, width)

        val targetStops = gradientStops.map { it.offset to withAlpha(it.color, targetGaugeAlpha) }
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
            barWidthPx = width,
            barHeightPx = thickness,
            filledWidthPx = targetFilledWidth,
            stops = targetStops,
        )

        drawMultiStopGradientGauge(
            guiGraphics = guiGraphics,
            barLeftX = padding,
            barTopY = padding,
            barWidthPx = width,
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
}
