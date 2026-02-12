package io.github.oni0nfr1.dynamicrider.client.hud.scenes.impl

import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics

interface HudScene {

    val elements: Collection<HudElement>
    val stateManager: HudStateManager

    fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        elements.forEach { it.draw(guiGraphics, deltaTracker) }
    }

    fun enable()
    fun disable()

}