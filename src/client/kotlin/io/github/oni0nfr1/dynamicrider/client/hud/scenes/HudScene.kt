package io.github.oni0nfr1.dynamicrider.client.hud.scenes

import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics

interface HudScene {

    val elements: MutableSet<HudElement>
    val stateManager: HudStateManager

    fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        elements.forEach { it.draw(guiGraphics, deltaTracker) }
    }

    fun enable()
    fun disable()

}