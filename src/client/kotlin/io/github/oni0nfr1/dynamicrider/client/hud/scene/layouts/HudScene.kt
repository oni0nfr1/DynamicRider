package io.github.oni0nfr1.dynamicrider.client.hud.scene.layouts

import io.github.oni0nfr1.dynamicrider.client.hud.elements.dsl.HUDSL
import io.github.oni0nfr1.dynamicrider.client.hud.elements.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics

@HUDSL
class HudScene<E: KartEngine>(private val kart: KartRef.Specific<E>) {

    private var elementSpecs: MutableList<HudElementSpec<*, E>> = mutableListOf()
    private val onEnableCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val onDisableCallbacks: MutableList<() -> Unit> = mutableListOf()

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

    fun onEnable(block: () -> Unit) {
        onEnableCallbacks += block
    }

    fun onDisable(block: () -> Unit) {
        onDisableCallbacks += block
    }

    private fun createElements(): List<HudElement<E>> = elementSpecs.map { it.create(kart) }

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
    = HudScene(kart).apply(block)

@HUDSL
class HudSceneDefinitionBuilder<E: KartEngine> {
    private val elementSpecs: MutableList<HudElementSpec<*, E>> = mutableListOf()

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

    fun build(): HudSceneDefinition<E> = HudSceneDefinition(elementSpecs.toList())
}

data class HudSceneDefinition<E: KartEngine>(
    val specs: List<HudElementSpec<*, E>>,
) {
    fun create(kart: KartRef.Specific<E>): HudScene<E> =
        HudScene(kart).apply {
            specs.forEach { addSpec(it) }
        }
}

fun <E: KartEngine> hudSceneDefinition(
    block: HudSceneDefinitionBuilder<E>.() -> Unit,
): HudSceneDefinition<E> =
    HudSceneDefinitionBuilder<E>().apply(block).build()
