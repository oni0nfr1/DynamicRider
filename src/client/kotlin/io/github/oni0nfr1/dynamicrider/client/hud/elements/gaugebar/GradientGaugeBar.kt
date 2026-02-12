package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar

import com.mojang.math.Axis
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.util.colorFromRGB
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import kotlin.math.exp

class GradientGaugeBar(
    manager: HudStateManager,
    composer: GradientGaugeBar.() -> Unit
) : HudElementImpl<GradientGaugeBar>(manager, composer), GaugeBar {

    var thickness = 8
    var width = 120
    var padding = 2

    var boxColor = 0x80000000.toInt()

    var gaugeAlpha = 0xFF
    var targetGaugeAlpha = 0x80

    /** 보간 속도(클수록 더 빨리 따라감). */
    var smoothing = 1.0

    var gradientStops: List<Pair<Int, Int>> = listOf(
        0   to colorFromRGB(255, 255, 255),
        30  to colorFromRGB(255, 232, 161),
        60  to colorFromRGB(255, 192, 64),
        90  to colorFromRGB(255, 94, 24),
        120 to colorFromRGB(255, 0, 0),
    )

    private var targetGauge = 0.0
    private var displayGauge = 0.0

    override var gauge: Double
        get() = targetGauge
        set(value) {
            targetGauge = value.coerceIn(0.0, 1.0)
        }

    override fun resolveSize() {
        setSize(width + padding * 2, thickness + padding * 2)
    }

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        val dtTicks = deltaTracker.realtimeDeltaTicks.toDouble()
        val follow = 1.0 - exp(-smoothing * dtTicks)
        displayGauge += (targetGauge - displayGauge) * follow
        displayGauge = displayGauge.coerceIn(0.0, 1.0)

        guiGraphics.fill(0, 0, size.x, size.y, boxColor)

        val barLeftX = padding
        val barTopY = padding
        val barWidthPx = width
        val barHeightPx = thickness

        val targetFilledWidth = (barWidthPx * targetGauge).toInt().coerceIn(0, barWidthPx)
        val filledWidth = (barWidthPx * displayGauge).toInt().coerceIn(0, barWidthPx)

        val targetStops = gradientStops.map { (off, color) -> off to withAlpha(color, targetGaugeAlpha) }
        val gaugeStops  = gradientStops.map { (off, color) -> off to withAlpha(color, gaugeAlpha) }

        drawMultiStopGradientGauge(
            guiGraphics = guiGraphics,
            barLeftX = barLeftX,
            barTopY = barTopY,
            barWidthPx = barWidthPx,
            barHeightPx = barHeightPx,
            filledWidthPx = targetFilledWidth,
            stops = targetStops
        )

        drawMultiStopGradientGauge(
            guiGraphics = guiGraphics,
            barLeftX = barLeftX,
            barTopY = barTopY,
            barWidthPx = barWidthPx,
            barHeightPx = barHeightPx,
            filledWidthPx = filledWidth,
            stops = gaugeStops
        )
    }

    private fun drawMultiStopGradientGauge(
        guiGraphics: GuiGraphics,
        barLeftX: Int,
        barTopY: Int,
        barWidthPx: Int,
        barHeightPx: Int,
        filledWidthPx: Int,
        stops: List<Pair<Int, Int>>
    ) {
        if (barWidthPx <= 0 || barHeightPx <= 0) return
        if (filledWidthPx <= 0) return
        if (stops.size < 2) return

        val barRightXExclusive = barLeftX + barWidthPx
        val filledRightXExclusive = (barLeftX + filledWidthPx).coerceAtMost(barRightXExclusive)

        // 오프셋 오름차순 정렬 + 0 이상 보정
        val sorted = stops
            .map { (offset, color) -> offset.coerceAtLeast(0) to color }
            .sortedBy { it.first }

        // 시작점이 0이 아니면 0을 강제로 넣어줌(안 넣으면 왼쪽이 비는 구간 생김)
        val normalized = if (sorted.first().first != 0) {
            listOf(0 to sorted.first().second) + sorted
        } else sorted

        // 각 구간을 차례대로 그리기
        for (i in 0 until normalized.size - 1) {
            val (offA, colorA) = normalized[i]
            val (offB, colorB) = normalized[i + 1]

            val xA = (barLeftX + offA).coerceIn(barLeftX, barRightXExclusive)
            val xB = (barLeftX + offB).coerceIn(barLeftX, barRightXExclusive)

            // 이 구간이 채워진 범위 밖이면 스킵
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
                rightColor = colorB
            )
        }

        // 마지막 스톱 이후는 마지막 색으로 고정(채워진 부분까지)
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
        rightColor: Int
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