package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar

import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.Composer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics

class PlainGaugeBar(
    stateManager: HudStateManager,
    composer: Composer<PlainGaugeBar>,
): HudElementImpl<PlainGaugeBar>(stateManager, composer), GaugeBar {

    var width: Float = 120f
    var thickness: Float = 5f

    var padding = 0
    var gaugeColor = 0xFF00C800.toInt()
    var boxColor = 0x80000000.toInt()

    override var gauge: Double = 0.0

    override fun resolveSize() {
        val widthInt = this.width.toInt()
        val thicknessInt = this.thickness.toInt()

        setSize(widthInt + padding * 2, thicknessInt + padding * 2)
    }

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        guiGraphics.fill(0, 0, size.x, size.y, boxColor)

        val widthInt = this.width.toInt()
        val thicknessInt = this.thickness.toInt()

        val filled = (widthInt * gauge).toInt().coerceIn(0, widthInt)
        guiGraphics.fill(
            padding,
            padding,
            padding + filled,
            padding + thicknessInt,
            gaugeColor
        )
    }
}