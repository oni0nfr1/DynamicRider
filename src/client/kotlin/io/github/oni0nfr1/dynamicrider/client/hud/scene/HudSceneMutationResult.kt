package io.github.oni0nfr1.dynamicrider.client.hud.scene

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidationError

/** ID 기반 HUD scene 요소 mutation 결과다. */
sealed interface HudSceneMutationResult {
    data object Applied : HudSceneMutationResult

    data class ElementNotFound(
        val elementId: String,
    ) : HudSceneMutationResult

    data class DuplicateElementId(
        val elementId: String,
    ) : HudSceneMutationResult

    data class IncompatibleState(
        val requiredStateClass: Class<out KartState>,
        val sceneStateClass: Class<out KartState>,
        val specType: String,
    ) : HudSceneMutationResult

    data class InvalidSpec(
        val errors: List<HudSpecValidationError>,
    ) : HudSceneMutationResult {
        init {
            require(errors.isNotEmpty()) { "Invalid HUD spec must contain at least one error" }
        }
    }
}
