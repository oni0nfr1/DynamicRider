package io.github.oni0nfr1.dynamicrider.client.hud.elements

import io.github.oni0nfr1.dynamicrider.client.hud.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.GaugeBar
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import kotlin.math.exp

class PlainGaugeBar(
    manager: HudStateManager,
    composer: PlainGaugeBar.() -> Unit
) : HudElementImpl<PlainGaugeBar>(manager, composer), GaugeBar {

    var thickness = 8
    var width = 125
    var padding = 2
    var boxColor = 0x80000000.toInt()
    var gaugeColor = 0xFFFFAA00.toInt()
    var targetGaugeColor = 0x80FFAA00.toInt()

    /** 보간 속도(클수록 더 빨리 따라감). 8~20 정도가 무난 */
    var smoothing = 1.0

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
        val alpha = 1.0 - exp(-smoothing * dtTicks)
        displayGauge += (targetGauge - displayGauge) * alpha
        displayGauge = displayGauge.coerceIn(0.0, 1.0)

        // 배경
        guiGraphics.fill(0, 0, size.x, size.y, boxColor)

        val filled = (width * displayGauge).toInt().coerceIn(0, width)
        val targetFilled = (width * targetGauge).toInt().coerceIn(0, width)
        guiGraphics.fill(
            padding,
            padding,
            padding + targetFilled,
            padding + thickness,
            targetGaugeColor,
        )
        guiGraphics.fill(
            padding,
            padding,
            padding + filled,
            padding + thickness,
            gaugeColor
        )
    }
}