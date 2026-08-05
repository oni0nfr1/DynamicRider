package io.github.oni0nfr1.dynamicrider.client.hud.editor.command

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentElement
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentChange
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath
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

/** 연속 입력을 최초 상태 하나로 되돌릴 수 있는 단일 history 항목으로 합칠 수 있는 명령이다. */
interface HudMergeableEditCommand : HudEditCommand {
    /** [newer]를 이 명령에 합쳐 즉시 적용하고, 호환되지 않으면 `null`을 반환한다. */
    fun mergeAndApply(newer: HudEditCommand, document: HudSceneDocument): HudDocumentChange?
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
    replacement: HudElementSpec<*, *>,
    mergeKey: HudPath? = null,
) : HudMergeableEditCommand {
    private var mergeKey: Set<HudPath>? = mergeKey?.let(::setOf)
    private var replacement: HudElementSpec<*, *> = replacement
    private var change: HudDocumentChange.SpecReplaced? = null

    override fun apply(document: HudSceneDocument): HudDocumentChange =
        document.replace(elementId, replacement).also { change = it }

    override fun revert(document: HudSceneDocument): HudDocumentChange = document.replace(
        elementId,
        checkNotNull(change) { "Command has not been applied" }.previous,
    )

    override fun mergeAndApply(newer: HudEditCommand, document: HudSceneDocument): HudDocumentChange? {
        if (newer !is ReplaceElementSpecCommand || mergeKey == null ||
            newer.elementId != elementId || newer.mergeKey != mergeKey
        ) {
            return null
        }
        replacement = newer.replacement
        return document.replace(elementId, replacement)
    }

    companion object {
        /** 동일한 [paths]를 갱신하는 후속 command와 병합 가능한 Spec 교체를 생성한다. */
        fun mergingLeaves(
            elementId: String,
            replacement: HudElementSpec<*, *>,
            paths: Set<HudPath>,
        ): ReplaceElementSpecCommand = ReplaceElementSpecCommand(elementId, replacement).also {
            require(paths.isNotEmpty()) { "Merge paths must not be empty" }
            it.mergeKey = paths.toSet()
        }
    }
}
