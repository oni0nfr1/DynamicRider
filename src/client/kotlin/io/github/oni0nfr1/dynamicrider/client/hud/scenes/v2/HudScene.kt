package io.github.oni0nfr1.dynamicrider.client.hud.scenes.v2

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl.HUDSL
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics

@HUDSL
class HudScene internal constructor() {

    val elementSpecs: MutableList<HudElementSpec<HudElement>> = mutableListOf()
    private val onEnableCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val onDisableCallbacks: MutableList<() -> Unit> = mutableListOf()

    var elements: List<HudElement> = mutableListOf()

    inline fun <reified BUILDER, SPEC> element(block: BUILDER.() -> Unit)
        where
            BUILDER : HudElementBuilder<SPEC>,
            SPEC : HudElementSpec<HudElement> {
        val builder = BUILDER::class.java.getDeclaredConstructor().newInstance()
        elementSpecs += builder.apply(block).build()
    }

    fun onEnable(block: () -> Unit) {
        onEnableCallbacks += block
    }

    fun onDisable(block: () -> Unit) {
        onDisableCallbacks += block
    }

    fun createElements(): List<HudElement> = elementSpecs.map { it.create() }

    fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        elements.forEach { it.draw(guiGraphics, deltaTracker) }
    }

    internal fun enable() {
        onEnableCallbacks.forEach { it() }
        elements = createElements()
    }

    internal fun disable() {
        onDisableCallbacks.forEach { it() }
    }
}

fun hudScene(block: HudScene.() -> Unit): HudScene = HudScene().apply(block)
