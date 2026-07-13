package io.github.oni0nfr1.dynamicrider.client.hud.editor.command

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument

class HudCommandStack(
    private val document: HudSceneDocument,
) {
    private val undoStack = ArrayDeque<HudEditCommand>()
    private val redoStack = ArrayDeque<HudEditCommand>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun execute(command: HudEditCommand) {
        command.apply(document)
        undoStack.addLast(command)
        redoStack.clear()
    }

    fun undo(): Boolean {
        val command = undoStack.removeLastOrNull() ?: return false
        command.revert(document)
        redoStack.addLast(command)
        return true
    }

    fun redo(): Boolean {
        val command = redoStack.removeLastOrNull() ?: return false
        command.apply(document)
        undoStack.addLast(command)
        return true
    }
}
