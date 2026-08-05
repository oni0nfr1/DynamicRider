package io.github.oni0nfr1.dynamicrider.client.hud.editor.service

import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.ReplaceElementSpecCommand
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyEditor
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

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
        path: HudPath,
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

    /** 같은 root 요소에 속한 primitive leaf 변경들을 하나의 command로 만든다. */
    fun createLeafChangeCommand(
        elementId: String,
        vararg changes: Pair<HudPath, JsonPrimitive>,
    ): HudSpecEditCommandResult {
        val element = document.elementById(elementId)
            ?: return HudSpecEditCommandResult.ElementNotFound(elementId)

        return when (val result = HudSpecPropertyEditor.updateLeaf(element.spec, *changes)) {
            is HudSpecPropertyUpdateResult.Success -> {
                if (result.spec == element.spec) {
                    HudSpecEditCommandResult.Unchanged(elementId)
                } else {
                    HudSpecEditCommandResult.Created(
                        ReplaceElementSpecCommand.mergingLeaves(
                            elementId = elementId,
                            replacement = result.spec,
                            paths = changes.map(Pair<HudPath, JsonPrimitive>::first).toSet(),
                        ),
                    )
                }
            }
            is HudSpecPropertyUpdateResult.Failure -> HudSpecEditCommandResult.PropertyRejected(result)
        }
    }

    /** [elementId]의 sealed property를 지정 subtype으로 바꾸는 command를 생성한다. */
    fun createVariantChangeCommand(
        elementId: String,
        path: HudPath,
        variantSerialName: String,
    ): HudSpecEditCommandResult {
        val element = document.elementById(elementId)
            ?: return HudSpecEditCommandResult.ElementNotFound(elementId)

        return when (val result = HudSpecPropertyEditor.changeVariant(element.spec, path, variantSerialName)) {
            is HudSpecPropertyUpdateResult.Success -> {
                if (result.spec == element.spec) {
                    HudSpecEditCommandResult.Unchanged(elementId)
                } else {
                    HudSpecEditCommandResult.Created(ReplaceElementSpecCommand(elementId, result.spec))
                }
            }
            is HudSpecPropertyUpdateResult.Failure -> HudSpecEditCommandResult.PropertyRejected(result)
        }
    }

    /** [elementId]의 nullable property를 비활성화하거나 기본값으로 활성화하는 command를 생성한다. */
    fun createPresenceChangeCommand(
        elementId: String,
        path: HudPath,
        present: Boolean,
    ): HudSpecEditCommandResult {
        val element = document.elementById(elementId)
            ?: return HudSpecEditCommandResult.ElementNotFound(elementId)

        return when (val result = HudSpecPropertyEditor.setPresence(element.spec, path, present)) {
            is HudSpecPropertyUpdateResult.Success -> {
                if (result.spec == element.spec) {
                    HudSpecEditCommandResult.Unchanged(elementId)
                } else {
                    HudSpecEditCommandResult.Created(ReplaceElementSpecCommand(elementId, result.spec))
                }
            }
            is HudSpecPropertyUpdateResult.Failure -> HudSpecEditCommandResult.PropertyRejected(result)
        }
    }
}
