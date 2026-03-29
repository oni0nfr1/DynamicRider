package io.github.oni0nfr1.dynamicrider.client.hud.scenes

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl.someMountedScene
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.impl.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.rider.KartEngine
import io.github.oni0nfr1.dynamicrider.client.util.DynRiderJvmFlags

fun mountSceneByEngine(stateManager: HudStateManager, engine: KartEngine?): HudScene {
    if (DynRiderJvmFlags.devMode) {
        return someMountedScene(stateManager)
    }

    return when (engine) {
        else -> DefaultScene(stateManager)
    }
}
