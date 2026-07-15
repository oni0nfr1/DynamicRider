package io.github.oni0nfr1.dynamicrider.client.hud.editor.command

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentChange
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec

/** document 변경 명령의 실행 순서와 undo/redo 기록을 관리한다. */
class HudCommandStack(
    private val document: HudSceneDocument,
) {
    private val undoStack = ArrayDeque<HudEditCommand>()
    private val redoStack = ArrayDeque<HudEditCommand>()
    private val changeListeners = LinkedHashSet<(HudDocumentChange) -> Unit>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    /**
     * command 실행, undo 및 redo로 발생한 document 변경을 받을 listener를 등록한다.
     *
     * Listener는 command history가 갱신된 뒤 동기적으로 호출되며 예외를 던지면 안 된다.
     * 반환된 handle을 닫으면 이후 이벤트를 받지 않는다.
     */
    fun addChangeListener(listener: (HudDocumentChange) -> Unit): AutoCloseable {
        changeListeners += listener
        return AutoCloseable { changeListeners -= listener }
    }

    /** 명령을 적용하고 undo 기록에 추가하며 기존 redo 기록을 폐기한다. */
    fun execute(command: HudEditCommand) {
        val change = command.apply(document)
        undoStack.addLast(command)
        redoStack.clear()
        publish(change)
    }

    /**
     * 마지막 명령을 되돌린다.
     *
     * @return 되돌린 명령이 있으면 `true`, 기록이 비어 있으면 `false`
     */
    fun undo(): Boolean {
        val command = undoStack.lastOrNull() ?: return false
        val change = command.revert(document)
        undoStack.removeLast()
        redoStack.addLast(command)
        publish(change)
        return true
    }

    /**
     * 마지막으로 되돌린 명령을 다시 적용한다.
     *
     * @return 다시 적용한 명령이 있으면 `true`, 기록이 비어 있으면 `false`
     */
    fun redo(): Boolean {
        val command = redoStack.lastOrNull() ?: return false
        val change = command.apply(document)
        redoStack.removeLast()
        undoStack.addLast(command)
        publish(change)
        return true
    }

    /** 저장소에서 다시 읽은 [spec]으로 document를 교체하고 기존 undo/redo 기록을 폐기한다. */
    fun reset(spec: HudSceneSpec) {
        val change = document.reset(spec)
        undoStack.clear()
        redoStack.clear()
        publish(change)
    }

    private fun publish(change: HudDocumentChange) {
        changeListeners.toList().forEach { it(change) }
    }
}
