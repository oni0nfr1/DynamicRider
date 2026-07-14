package io.github.oni0nfr1.dynamicrider.client.hud.scene

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState

sealed interface HudSceneSpecAddResult {
    data object Added : HudSceneSpecAddResult

    data class IncompatibleState(
        val requiredStateClass: Class<out KartState>,
        val sceneStateClass: Class<out KartState>,
        val specType: String,
    ) : HudSceneSpecAddResult
}
