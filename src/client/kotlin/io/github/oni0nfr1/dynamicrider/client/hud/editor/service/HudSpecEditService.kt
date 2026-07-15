package io.github.oni0nfr1.dynamicrider.client.hud.editor.service

import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.ReplaceElementSpecCommand
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyEditor
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult
import kotlinx.serialization.json.JsonElement

/** document의 현재 Spec에 대한 property 변경 요청을 검증된 command로 변환한다. */
class HudSpecEditService(
    private val document: HudSceneDocument,
) {
    /**
     * [elementId]의 [path]에 [value]를 적용하는 command를 생성한다.
     *
     * 이 함수는 command를 실행하거나 document를 변경하지 않는다. 호출자는 성공 결과의 command를
     * `HudCommandStack`으로 즉시 실행해야 한다.
     */
    fun createPropertyChangeCommand(
        elementId: String,
        path: HudPropertyPath,
        value: JsonElement,
    ): HudSpecEditCommandResult {
        val element = document.elementById(elementId)
            ?: return HudSpecEditCommandResult.ElementNotFound(elementId)

        return when (val result = HudSpecPropertyEditor.update(element.spec, path, value)) {
            is HudSpecPropertyUpdateResult.Success -> {
                if (result.spec == element.spec) {
                    HudSpecEditCommandResult.Unchanged(elementId)
                } else {
                    HudSpecEditCommandResult.Created(
                        ReplaceElementSpecCommand(elementId, result.spec, path)
                    )
                }
            }
            is HudSpecPropertyUpdateResult.Failure -> HudSpecEditCommandResult.PropertyRejected(result)
        }
    }
}
