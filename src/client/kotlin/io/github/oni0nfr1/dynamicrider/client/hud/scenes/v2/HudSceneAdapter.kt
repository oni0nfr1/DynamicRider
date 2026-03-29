package io.github.oni0nfr1.dynamicrider.client.hud.scenes.v2

import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.impl.HudScene as LegacyHudScene

private class HudSceneAdapter(
    private val scene: HudScene,
    override val stateManager: HudStateManager,
) : LegacyHudScene {
    private val runtimeElements: List<HudElement> by lazy(scene::createElements)

    override val elements: Collection<HudElement>
        get() = runtimeElements

    override fun enable() {
        scene.enable()
        runtimeElements
    }

    override fun disable() {
        scene.disable()
    }
}

fun HudScene.mount(stateManager: HudStateManager): LegacyHudScene =
    HudSceneAdapter(this, stateManager)
