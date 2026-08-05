package io.github.oni0nfr1.dynamicrider.client.hud.elements.impl

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.HeldHudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import kotlin.reflect.KProperty0

abstract class CompoundElement<S : KartState>(
    layout: HudLayoutSpec,
    context: HudSceneContext<S>,
    parent: ElementHolder
) : HudElementImpl<S>(layout, context, parent), ElementHolder {

    private val childSpecs: MutableList<Pair<String, HudElementSpec<*, S>>> = mutableListOf()

    private val children: List<Pair<String, HudElement<S>>> by lazy {
        childSpecs.map { (key, spec) -> key to spec.create(context, this) }
    }

    final override val heldElements: List<HeldHudElement>
        get() = children.map { (key, element) -> HeldHudElement(key, element) }

    protected fun addChild(property: KProperty0<HudElementSpec<*, S>?>) {
        require(childSpecs.none { (key) -> key == property.name }) {
            "Duplicate compound HUD child key '${property.name}'"
        }
        property.get()?.let { childSpecs += property.name to it }
    }

    final override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        renderMain(guiGraphics, deltaTracker)
        children.forEach { (_, element) -> element.draw(guiGraphics, deltaTracker) }
    }

    protected open fun renderMain(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {}
}
