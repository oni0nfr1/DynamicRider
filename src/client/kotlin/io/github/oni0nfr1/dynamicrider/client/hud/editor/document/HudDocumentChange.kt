package io.github.oni0nfr1.dynamicrider.client.hud.editor.document

import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec

/** command 적용 또는 복원으로 document에 실제 발생한 단발성 변경이다. */
sealed interface HudDocumentChange {
    data class Added(
        val element: HudDocumentElement,
        val index: Int,
    ) : HudDocumentChange

    data class Removed(
        val element: HudDocumentElement,
        val index: Int,
    ) : HudDocumentChange

    data class Moved(
        val elementId: String,
        val fromIndex: Int,
        val toIndex: Int,
    ) : HudDocumentChange

    data class SpecReplaced(
        val elementId: String,
        val previous: HudElementSpec<*, *>,
        val replacement: HudElementSpec<*, *>,
    ) : HudDocumentChange

    /** 저장소 재조회처럼 command history에 속하지 않는 전체 document 교체다. */
    data class Reset(
        val previous: HudSceneSpec,
        val replacement: HudSceneSpec,
    ) : HudDocumentChange
}
