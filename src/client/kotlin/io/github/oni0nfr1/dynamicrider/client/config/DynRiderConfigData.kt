package io.github.oni0nfr1.dynamicrider.client.config

import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.CustomSceneSelection
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HudUiMode
import kotlinx.serialization.Serializable

@Serializable
data class DynRiderConfigData(
    val isModEnabled: Boolean = true,
    val hudFont: String = "VANILLA",
    val uiMode: HudUiMode = HudUiMode.DEFAULT,
    val customScenes: Map<String, CustomSceneSelection> = emptyMap(),
)
