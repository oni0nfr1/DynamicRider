package io.github.oni0nfr1.dynamicrider.client.hud.scene

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.dsl.HUDSL
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

@HUDSL
class HudScene<E: KartEngine>(
    private val kart: KartRef.Specific<E>,
    private val engineClass: Class<E>,
) : ElementHolder {

    private var elementSpecs: MutableList<HudElementSpec<*, E>> = mutableListOf()
    private val onEnableCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val onDisableCallbacks: MutableList<() -> Unit> = mutableListOf()

    override val width: Int
        get() = Minecraft.getInstance().window.guiScaledWidth
    override val height: Int
        get() = Minecraft.getInstance().window.guiScaledHeight

    private var elements: List<HudElement<E>> = mutableListOf()

    inline fun <reified BUILDER> element(block: BUILDER.() -> Unit)
        where
            BUILDER : HudElementBuilder<HudElementSpec<*, E>> {
        val builder = BUILDER::class.java.getDeclaredConstructor().newInstance()
        addSpec(builder.apply(block).build())
    }

    fun <SPEC> addSpec(spec: SPEC)
        where
            SPEC : HudElementSpec<*, E> {
        elementSpecs += spec
    }

    fun addSpecChecked(spec: HudElementSpec<*, *>): HudSceneSpecAddResult {
        val requiredEngineClass = spec.requiredEngineClass()
        if (!requiredEngineClass.isAssignableFrom(engineClass)) {
            return HudSceneSpecAddResult.IncompatibleEngine(
                requiredEngineClass = requiredEngineClass,
                sceneEngineClass = engineClass,
                specType = spec::class.java.name,
            )
        }

        @Suppress("UNCHECKED_CAST")
        addSpec(spec as HudElementSpec<*, E>)
        return HudSceneSpecAddResult.Added
    }

    fun onEnable(block: () -> Unit) {
        onEnableCallbacks += block
    }

    fun onDisable(block: () -> Unit) {
        onDisableCallbacks += block
    }

    private fun createElements(): List<HudElement<E>> = elementSpecs.map { it.create(kart, this) }

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

inline fun <reified E: KartEngine> hudScene(kart: KartRef.Specific<E>, block: HudScene<E>.() -> Unit): HudScene<E>
    = HudScene(kart, E::class.java).apply(block)
