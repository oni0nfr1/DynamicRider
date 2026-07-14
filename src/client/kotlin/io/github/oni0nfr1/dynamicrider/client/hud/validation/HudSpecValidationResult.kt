package io.github.oni0nfr1.dynamicrider.client.hud.validation

/** HUD spec 전체에 대한 의미적 유효성 검사 결과다. */
sealed interface HudSpecValidationResult {
    data object Valid : HudSpecValidationResult

    data class Invalid(
        val errors: List<HudSpecValidationError>,
    ) : HudSpecValidationResult {
        init {
            require(errors.isNotEmpty()) { "Invalid HUD spec must contain at least one error" }
        }
    }
}

data class HudSpecValidationError(
    val path: HudSpecPath,
    val code: HudSpecValidationErrorCode,
    val message: String,
)

enum class HudSpecValidationErrorCode {
    UNREGISTERED_SPEC,
    SERIALIZATION_FAILURE,
    NON_FINITE_NUMBER,
    OUT_OF_RANGE,
}
