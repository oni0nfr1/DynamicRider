package io.github.oni0nfr1.dynamicrider.client.hud.scenes

import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.GaugeTracker
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.NitroCounter
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.Speedometer
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import kotlin.collections.forEach

interface HudScene_ {
    val elements: MutableSet<HudElement>
    val stateManager: HudStateManager

    fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        elements.forEach { it.draw(guiGraphics, deltaTracker) }

    }

    val gaugeTracker: GaugeTracker?
    val nitroCounter: NitroCounter?
    val speedometer:  Speedometer?
}
