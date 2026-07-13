package io.github.oni0nfr1.dynamicrider.client.hud.editor.command

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentElement
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec

interface HudEditCommand {
    fun apply(document: HudSceneDocument)
    fun revert(document: HudSceneDocument)
}

class AddElementCommand(
    private val element: HudDocumentElement,
    private val index: Int = Int.MAX_VALUE,
) : HudEditCommand {
    override fun apply(document: HudSceneDocument) = document.add(index, element)
    override fun revert(document: HudSceneDocument) { document.remove(element.id) }
}

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
