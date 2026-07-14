package io.github.oni0nfr1.dynamicrider.client.hud.editor.property

import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec

/** generic HUD spec property 변경 결과다. */
sealed interface HudSpecPropertyUpdateResult {
    /** 변경 및 검증을 통과해 생성된 새 immutable spec이다. */
    data class Success(
        val spec: HudElementSpec<*, *>,
    ) : HudSpecPropertyUpdateResult

    /** 변경할 수 없는 값이나 경로에 대한 실패다. */
    data class Failure(
        val path: HudPropertyPath,
        val reason: Reason,
        val message: String,
    ) : HudSpecPropertyUpdateResult

    enum class Reason {
        UNREGISTERED_SPEC,
        UNKNOWN_PROPERTY,
        UNSUPPORTED_PROPERTY,
        INVALID_VALUE,
    }
}
