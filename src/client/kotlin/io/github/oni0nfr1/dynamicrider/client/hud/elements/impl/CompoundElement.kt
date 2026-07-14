package io.github.oni0nfr1.dynamicrider.client.hud.elements.impl

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics

abstract class CompoundElement<S : KartState>(
    layout: HudLayoutSpec,
    context: HudSceneContext<S>,
    parent: ElementHolder
) : HudElementImpl<S>(layout, context, parent), ElementHolder {

    private val childSpecs: MutableList<HudElementSpec<*, S>> = mutableListOf()

    private val children: List<HudElement<S>> by lazy {
        childSpecs.map { it.create(context, this) }
    }

    protected fun addChild(spec: HudElementSpec<*, S>) {
        childSpecs += spec
    }

    final override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        renderMain(guiGraphics, deltaTracker)
        children.forEach { it.draw(guiGraphics, deltaTracker) }
    }

    protected open fun renderMain(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {}
}
