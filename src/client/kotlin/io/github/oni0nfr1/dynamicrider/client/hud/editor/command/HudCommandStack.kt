package io.github.oni0nfr1.dynamicrider.client.hud.editor.command

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument

/** document 변경 명령의 실행 순서와 undo/redo 기록을 관리한다. */
class HudCommandStack(
    private val document: HudSceneDocument,
) {
    private val undoStack = ArrayDeque<HudEditCommand>()
    private val redoStack = ArrayDeque<HudEditCommand>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    /** 명령을 적용하고 undo 기록에 추가하며 기존 redo 기록을 폐기한다. */
    fun execute(command: HudEditCommand) {
        command.apply(document)
        undoStack.addLast(command)
        redoStack.clear()
    }

    /**
     * 마지막 명령을 되돌린다.
     *
     * @return 되돌린 명령이 있으면 `true`, 기록이 비어 있으면 `false`
     */
    fun undo(): Boolean {
        val command = undoStack.removeLastOrNull() ?: return false
        command.revert(document)
        redoStack.addLast(command)
        return true
    }

    /**
     * 마지막으로 되돌린 명령을 다시 적용한다.
     *
     * @return 다시 적용한 명령이 있으면 `true`, 기록이 비어 있으면 `false`
     */
    fun redo(): Boolean {
        val command = redoStack.removeLastOrNull() ?: return false
        command.apply(document)
        undoStack.addLast(command)
        return true
    }
}
