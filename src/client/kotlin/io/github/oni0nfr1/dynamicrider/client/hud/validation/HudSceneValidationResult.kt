package io.github.oni0nfr1.dynamicrider.client.hud.validation

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState

/** 파일 및 loader 문맥과 무관한 HUD 장면 전체 검증 결과다. */
sealed interface HudSceneValidationResult {
    data object Valid : HudSceneValidationResult

    data class Invalid(
        val errors: List<HudSceneValidationError>,
    ) : HudSceneValidationResult {
        init {
            require(errors.isNotEmpty()) { "Invalid HUD scene must contain at least one error" }
        }
    }
}

/** 장면 안에서 실패한 요소의 위치와 원인을 나타내는 중립적인 검증 오류다. */
sealed interface HudSceneValidationError {
    val elementIndex: Int
    val elementId: String?
    val specType: String

    data class IncompatibleState(
        override val elementIndex: Int,
        override val elementId: String?,
        override val specType: String,
        val requiredStateClass: Class<out KartState>,
        val sceneStateClass: Class<out KartState>,
    ) : HudSceneValidationError

    data class InvalidSpec(
        override val elementIndex: Int,
        override val elementId: String?,
        override val specType: String,
        val errors: List<HudSpecValidationError>,
    ) : HudSceneValidationError {
        init {
            require(errors.isNotEmpty()) { "Invalid HUD element spec must contain at least one error" }
        }
    }
}
