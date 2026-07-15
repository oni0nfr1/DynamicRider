package io.github.oni0nfr1.dynamicrider.client.hud.editor.session

import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidationError

/** GUI가 요청한 하나의 session 작업 결과다. */
sealed interface HudEditorActionResult {
    /** 작업이 적용되었다. 새 요소 추가라면 [elementId]에 생성된 ID가 들어간다. */
    data class Applied(
        val elementId: String? = null,
    ) : HudEditorActionResult

    /** 요청이 현재 상태와 같거나 적용할 undo/redo 기록이 없다. */
    data object Unchanged : HudEditorActionResult

    data class ElementNotFound(
        val elementId: String,
    ) : HudEditorActionResult

    data class IncompatibleState(
        val requiredStateClass: Class<out KartState>,
        val sceneStateClass: Class<out KartState>,
        val specType: String,
    ) : HudEditorActionResult

    data class InvalidSpec(
        val errors: List<HudSpecValidationError>,
    ) : HudEditorActionResult

    data class PropertyRejected(
        val failure: HudSpecPropertyUpdateResult.Failure,
    ) : HudEditorActionResult

    /** 이미 닫힌 session에는 편집 요청을 적용할 수 없다. */
    data object Closed : HudEditorActionResult
}
