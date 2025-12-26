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

    /** 보간 속도(클수록 더 빨리 따라감). 8~20 정도가 무난 */
    var smoothing = 12.0

    private var targetGauge = 0.0      // 외부에서 세팅되는 “목표”
    private var displayGauge = 0.0     // 실제로 그릴 “표시”

    override var gauge: Double
        get() = targetGauge
        set(value) {
            targetGauge = value.coerceIn(0.0, 1.0)
            // 원하면 첫 프레임에 튀는 거 방지:
            // if (displayGauge == 0.0) displayGauge = targetGauge
        }

    override fun resolveSize() {
        setSize(width + padding * 2, thickness + padding * 2)
    }

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        // dt(틱 단위) 기반 지수 보간: FPS가 달라도 체감 속도 일정
        val dtTicks = deltaTracker.realtimeDeltaTicks.toDouble() // :contentReference[oaicite:1]{index=1}
        val alpha = 1.0 - exp(-smoothing * dtTicks)  // 0..1
        displayGauge += (targetGauge - displayGauge) * alpha
        displayGauge = displayGauge.coerceIn(0.0, 1.0)

        // 배경
        guiGraphics.fill(0, 0, size.x, size.y, boxColor)

        // 게이지 (좌표 버그도 같이 수정: x2/y2에 padding 더해줘야 함)
        val filled = (width * displayGauge).toInt().coerceIn(0, width)
        guiGraphics.fill(
            padding,
            padding,
            padding + filled,
            padding + thickness,
            gaugeColor
        )
    }
}