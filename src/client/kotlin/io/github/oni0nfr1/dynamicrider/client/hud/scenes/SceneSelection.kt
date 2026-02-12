package io.github.oni0nfr1.dynamicrider.client.hud.scenes

import io.github.oni0nfr1.dynamicrider.client.hud.scenes.impl.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.rider.KartEngine

fun mountSceneByEngine(stateManager: HudStateManager, engine: KartEngine?): HudScene {
    return when (engine) {
        else -> DefaultScene(stateManager)
    }
}