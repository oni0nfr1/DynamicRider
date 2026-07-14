package io.github.oni0nfr1.dynamicrider.client.hud.editor.command

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentElement
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec

/**
 * HUD document에 적용하고 되돌릴 수 있는 하나의 편집 작업이다.
 *
 * 명령 인스턴스는 [HudCommandStack]을 통해 실행해야 한다.
 */
interface HudEditCommand {
    /** 변경을 document에 적용한다. */
    fun apply(document: HudSceneDocument)

    /** 앞서 적용한 변경을 document에서 되돌린다. */
    fun revert(document: HudSceneDocument)
}

/** 지정한 위치에 요소를 추가하는 명령이다. */
class AddElementCommand(
    private val element: HudDocumentElement,
    private val index: Int = Int.MAX_VALUE,
) : HudEditCommand {
    override fun apply(document: HudSceneDocument) = document.add(index, element)
    override fun revert(document: HudSceneDocument) { document.remove(element.id) }
}

/** ID로 요소를 찾아 제거하는 명령이다. */
class RemoveElementCommand(
    private val elementId: String,
) : HudEditCommand {
    private var removed: Pair<Int, HudDocumentElement>? = null

    override fun apply(document: HudSceneDocument) {
        removed = document.remove(elementId)
    }

    override fun revert(document: HudSceneDocument) {
        val (index, element) = checkNotNull(removed) { "Command has not been applied" }
        document.add(index, element)
    }
}

/** 요소의 렌더 및 저장 순서를 변경하는 명령이다. */
class MoveElementCommand(
    private val elementId: String,
    private val targetIndex: Int,
) : HudEditCommand {
    private var sourceIndex: Int? = null

    override fun apply(document: HudSceneDocument) {
        sourceIndex = document.move(elementId, targetIndex)
    }

    override fun revert(document: HudSceneDocument) {
        document.move(elementId, checkNotNull(sourceIndex) { "Command has not been applied" })
    }
}

/** 요소 ID를 유지하면서 전체 명세를 교체하는 명령이다. */
class ReplaceElementSpecCommand(
    private val elementId: String,
    private val replacement: HudElementSpec<*, *>,
) : HudEditCommand {
    private var previous: HudElementSpec<*, *>? = null

    override fun apply(document: HudSceneDocument) {
        previous = document.replace(elementId, replacement)
    }

    override fun revert(document: HudSceneDocument) {
        document.replace(elementId, checkNotNull(previous) { "Command has not been applied" })
    }
}
