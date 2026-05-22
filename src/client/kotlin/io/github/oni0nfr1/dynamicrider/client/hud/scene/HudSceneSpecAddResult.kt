package io.github.oni0nfr1.dynamicrider.client.hud.scene

import io.github.oni0nfr1.skid.client.api.engine.KartEngine

sealed interface HudSceneSpecAddResult {
    data object Added : HudSceneSpecAddResult

    data class IncompatibleEngine(
        val requiredEngineClass: Class<out KartEngine>,
        val sceneEngineClass: Class<out KartEngine>,
        val specType: String,
    ) : HudSceneSpecAddResult
}
