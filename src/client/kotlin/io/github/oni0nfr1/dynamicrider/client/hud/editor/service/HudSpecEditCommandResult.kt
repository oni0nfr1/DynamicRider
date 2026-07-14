package io.github.oni0nfr1.dynamicrider.client.hud.editor.service

import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.ReplaceElementSpecCommand
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult

/** Spec property 변경 요청을 command로 변환한 결과다. */
sealed interface HudSpecEditCommandResult {
    /** 검증된 새 Spec을 적용할 command가 생성되었다. */
    data class Created(
        val command: ReplaceElementSpecCommand,
    ) : HudSpecEditCommandResult

    /** 요청한 값이 현재 Spec과 같아 command가 필요하지 않다. */
    data class Unchanged(
        val elementId: String,
    ) : HudSpecEditCommandResult

    /** 현재 document에 요청한 element ID가 존재하지 않는다. */
    data class ElementNotFound(
        val elementId: String,
    ) : HudSpecEditCommandResult

    /** property 변경 또는 새 Spec 검증에 실패했다. */
    data class PropertyRejected(
        val failure: HudSpecPropertyUpdateResult.Failure,
    ) : HudSpecEditCommandResult
}
