package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.skid.client.api.engine.KartEngine

sealed interface HudSceneLoadResult<out E : KartEngine> {
    data class Loaded<E : KartEngine>(
        val scene: HudScene<E>,
    ) : HudSceneLoadResult<E>

    data class Failed(
        val errors: List<HudSceneLoadError>,
    ) : HudSceneLoadResult<Nothing>
}
