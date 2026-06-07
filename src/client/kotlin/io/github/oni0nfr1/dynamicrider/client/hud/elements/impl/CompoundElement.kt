package io.github.oni0nfr1.dynamicrider.client.hud.elements.impl

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics

abstract class CompoundElement<E: KartEngine>(
    layout: HudLayoutSpec,
    kart: KartRef.Specific<E>,
    parent: ElementHolder
) : HudElementImpl<E>(layout, kart, parent), ElementHolder {

    private val childSpecs: MutableList<HudElementSpec<*, E>> = mutableListOf()

    private val children: List<HudElement<E>> by lazy {
        childSpecs.map { it.create(kart, this) }
    }

    protected fun addChild(spec: HudElementSpec<*, E>) {
        childSpecs += spec
    }

    final override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        renderMain(guiGraphics, deltaTracker)
        children.forEach { it.draw(guiGraphics, deltaTracker) }
    }

    protected abstract fun renderMain(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker)
}
