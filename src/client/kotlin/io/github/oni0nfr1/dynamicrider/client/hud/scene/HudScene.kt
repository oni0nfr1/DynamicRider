package io.github.oni0nfr1.dynamicrider.client.hud.scene

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidationResult
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidator
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

class HudScene<S : KartState>(
    private val context: HudSceneContext<S>,
) : ElementHolder {

    private var elementSpecs: MutableList<HudElementSpec<*, S>> = mutableListOf()
    private val onEnableCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val onDisableCallbacks: MutableList<() -> Unit> = mutableListOf()

    override val width: Int
        get() = Minecraft.getInstance().window.guiScaledWidth
    override val height: Int
        get() = Minecraft.getInstance().window.guiScaledHeight

    private var elements: List<HudElement<S>> = mutableListOf()

    fun addSpec(spec: HudElementSpec<*, *>): HudSceneSpecAddResult {
        val requiredStateClass = spec.requiredStateClass()
        val sceneStateClass = context.kartStateType.stateClass
        if (!requiredStateClass.isAssignableFrom(sceneStateClass)) {
            return HudSceneSpecAddResult.IncompatibleState(
                requiredStateClass = requiredStateClass,
                sceneStateClass = sceneStateClass,
                specType = spec::class.java.name,
            )
        }

        when (val validation = HudSpecValidator.validate(spec)) {
            HudSpecValidationResult.Valid -> Unit
            is HudSpecValidationResult.Invalid -> {
                return HudSceneSpecAddResult.InvalidSpec(validation.errors)
            }
        }

        @Suppress("UNCHECKED_CAST")
        elementSpecs += spec as HudElementSpec<*, S>
        return HudSceneSpecAddResult.Added
    }

    fun onEnable(block: () -> Unit) {
        onEnableCallbacks += block
    }

    fun onDisable(block: () -> Unit) {
        onDisableCallbacks += block
    }

    private fun createElements(): List<HudElement<S>> = elementSpecs.map { it.create(context, this) }

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
