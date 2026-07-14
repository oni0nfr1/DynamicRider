package io.github.oni0nfr1.dynamicrider.client.hud.editor.command

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentElement
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentChange
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec

/**
 * HUD document에 적용하고 되돌릴 수 있는 하나의 편집 작업이다.
 *
 * 명령 인스턴스는 [HudCommandStack]을 통해 실행해야 한다.
 */
interface HudEditCommand {
    /** 변경을 document에 적용한다. */
    fun apply(document: HudSceneDocument): HudDocumentChange

    /** 앞서 적용한 변경을 document에서 되돌린다. */
    fun revert(document: HudSceneDocument): HudDocumentChange
}

/** 지정한 위치에 요소를 추가하는 명령이다. */
class AddElementCommand(
    private val element: HudDocumentElement,
    private val index: Int = Int.MAX_VALUE,
) : HudEditCommand {
    override fun apply(document: HudSceneDocument): HudDocumentChange = document.add(index, element)
    override fun revert(document: HudSceneDocument): HudDocumentChange = document.remove(element.id)
}

/** ID로 요소를 찾아 제거하는 명령이다. */
class RemoveElementCommand(
    private val elementId: String,
) : HudEditCommand {
    private var removed: HudDocumentChange.Removed? = null

    override fun apply(document: HudSceneDocument): HudDocumentChange =
        document.remove(elementId).also { removed = it }

    override fun revert(document: HudSceneDocument): HudDocumentChange {
        val change = checkNotNull(removed) { "Command has not been applied" }
        return document.add(change.index, change.element)
    }
}

/** 요소의 렌더 및 저장 순서를 변경하는 명령이다. */
class MoveElementCommand(
    private val elementId: String,
    private val targetIndex: Int,
) : HudEditCommand {
    private var movement: HudDocumentChange.Moved? = null

    override fun apply(document: HudSceneDocument): HudDocumentChange =
        document.move(elementId, targetIndex).also { movement = it }

    override fun revert(document: HudSceneDocument): HudDocumentChange = document.move(
        elementId,
        checkNotNull(movement) { "Command has not been applied" }.fromIndex,
    )
}

/** 요소 ID를 유지하면서 전체 명세를 교체하는 명령이다. */
class ReplaceElementSpecCommand(
    private val elementId: String,
    private val replacement: HudElementSpec<*, *>,
) : HudEditCommand {
    private var change: HudDocumentChange.SpecReplaced? = null

    override fun apply(document: HudSceneDocument): HudDocumentChange =
        document.replace(elementId, replacement).also { change = it }

    override fun revert(document: HudSceneDocument): HudDocumentChange = document.replace(
        elementId,
        checkNotNull(change) { "Command has not been applied" }.previous,
    )
}
