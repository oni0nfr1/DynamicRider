package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState

sealed interface HudSceneLoadResult<out S : KartState> {
    data class Loaded<S : KartState>(
        val scene: HudScene<S>,
    ) : HudSceneLoadResult<S>

    data class Failed(
        val errors: List<HudSceneLoadError>,
    ) : HudSceneLoadResult<Nothing>
}
