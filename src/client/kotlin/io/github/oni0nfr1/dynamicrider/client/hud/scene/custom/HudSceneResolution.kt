package io.github.oni0nfr1.dynamicrider.client.hud.scene.custom

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.skid.client.api.engine.KartEngine

enum class HudSceneSource {
    CUSTOM_CONFIG,
    RESOURCE,
    CUSTOM_FALLBACK,
}

sealed interface HudSceneResolution<out E : KartEngine> {
    data class Resolved<E : KartEngine>(
        val scene: HudScene<E>,
        val source: HudSceneSource,
        val diagnostics: List<HudSceneLoadError> = emptyList(),
    ) : HudSceneResolution<E>

    data class Failed(
        val errors: List<HudSceneLoadError>,
    ) : HudSceneResolution<Nothing>
}
