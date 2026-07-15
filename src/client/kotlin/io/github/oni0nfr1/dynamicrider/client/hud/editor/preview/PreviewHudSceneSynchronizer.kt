package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.HudCommandStack
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentChange
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneMutationResult
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState

/** document snapshot과 단발성 change를 하나의 preview runtime scene에 동기화한다. */
class PreviewHudSceneSynchronizer<S : KartState>(
    private val document: HudSceneDocument,
    commandStack: HudCommandStack,
    val scene: HudScene<S>,
) : AutoCloseable {
    private val subscription: AutoCloseable
    private var closed: Boolean = false

    /** 자동 복구까지 실패한 마지막 동기화 오류다. */
    var lastFailure: Throwable? = null
        private set

    init {
        rebuildFromDocument()
        scene.enable()
        subscription = commandStack.addChangeListener(::onDocumentChange)
    }

    /** 현재 document snapshot으로 같은 scene 인스턴스의 모든 entry를 다시 구성한다. */
    fun resynchronize(): Result<Unit> = runCatching {
        check(!closed) { "Preview HUD scene synchronizer is closed" }
        rebuildFromDocument()
        lastFailure = null
    }.onFailure { lastFailure = it }

    override fun close() {
        if (closed) return
        subscription.close()
        scene.disable()
        closed = true
    }

    private fun onDocumentChange(change: HudDocumentChange) {
        if (closed) return
        val incrementalFailure = runCatching { apply(change) }.exceptionOrNull()
        if (incrementalFailure == null) {
            lastFailure = null
            return
        }

        val recoveryFailure = runCatching { rebuildFromDocument() }.exceptionOrNull()
        lastFailure = recoveryFailure?.also { it.addSuppressed(incrementalFailure) }
    }

    private fun apply(change: HudDocumentChange) {
        val result = when (change) {
            is HudDocumentChange.Added -> scene.addElement(
                change.element.id,
                change.element.spec,
                change.index,
            )
            is HudDocumentChange.Removed -> scene.removeElement(change.element.id)
            is HudDocumentChange.Moved -> scene.moveElement(change.elementId, change.toIndex)
            is HudDocumentChange.SpecReplaced -> scene.replaceElement(change.elementId, change.replacement)
            is HudDocumentChange.Reset -> {
                rebuildFromDocument()
                return
            }
        }
        result.requireApplied("apply $change")
    }

    private fun rebuildFromDocument() {
        scene.clearElements()
        document.elements.forEachIndexed { index, element ->
            scene.addElement(element.id, element.spec, index)
                .requireApplied("add '${element.id}' while rebuilding preview scene")
        }
    }

    private fun HudSceneMutationResult.requireApplied(operation: String) {
        check(this == HudSceneMutationResult.Applied) {
            "Failed to $operation: $this"
        }
    }
}
