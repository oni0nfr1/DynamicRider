package io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector

/** Document 요소의 현재 Spec을 GUI 편집 모델로 읽은 결과다. */
sealed interface HudElementInspectionResult {
    data class Inspected(
        val model: HudElementEditorModel,
    ) : HudElementInspectionResult

    data class ElementNotFound(
        val elementId: String,
    ) : HudElementInspectionResult

    /** nullable 요소 슬롯은 존재하지만 현재 값이 `null`이라 Inspector 내용이 없는 상태다. */
    data class AbsentElement(
        val path: io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath,
        val nameKey: String,
        val typeId: String?,
    ) : HudElementInspectionResult

    data class UnregisteredSpec(
        val elementId: String,
        val specType: String,
    ) : HudElementInspectionResult

    data class EncodingFailed(
        val elementId: String,
        val specType: String,
        val cause: Exception,
    ) : HudElementInspectionResult
}
