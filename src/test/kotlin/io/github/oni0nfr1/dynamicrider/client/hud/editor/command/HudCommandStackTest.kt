package io.github.oni0nfr1.dynamicrider.client.hud.editor.command

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentElement
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentChange
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HudCommandStackTest {
    @Test
    fun `undo and redo restore document order and specs`() {
        val first = GradientGaugeBar.Spec(width = 100)
        val second = GradientGaugeBar.Spec(width = 200)
        val replacement = GradientGaugeBar.Spec(width = 300)
        val document = HudSceneDocument.from(
            HudSceneSpec(elementIds = listOf("first", "second"), elements = listOf(first, second))
        )
        val commands = HudCommandStack(document)

        commands.execute(MoveElementCommand("first", 1))
        commands.execute(ReplaceElementSpecCommand("second", replacement))

        assertEquals(listOf("second", "first"), document.elements.map { it.id })
        assertEquals(replacement, document.elements.first().spec)
        assertTrue(commands.undo())
        assertEquals(second, document.elements.first().spec)
        assertTrue(commands.undo())
        assertEquals(listOf("first", "second"), document.elements.map { it.id })

        assertTrue(commands.redo())
        assertTrue(commands.redo())
        assertEquals(listOf("second", "first"), document.elements.map { it.id })
        assertEquals(replacement, document.elements.first().spec)
    }

    @Test
    fun `executing a new command clears redo history`() {
        val document = HudSceneDocument.from(HudSceneSpec(elements = emptyList()))
        val commands = HudCommandStack(document)

        commands.execute(AddElementCommand(HudDocumentElement("first", GradientGaugeBar.Spec())))
        assertTrue(commands.undo())
        commands.execute(AddElementCommand(HudDocumentElement("second", GradientGaugeBar.Spec())))

        assertFalse(commands.canRedo)
        assertFalse(commands.redo())
        assertEquals(listOf("second"), document.elements.map { it.id })
    }

    @Test
    fun `compatible replacements merge into one undo entry`() {
        val original = GradientGaugeBar.Spec(width = 100)
        val intermediate = GradientGaugeBar.Spec(width = 200)
        val final = GradientGaugeBar.Spec(width = 300)
        val document = HudSceneDocument.from(
            HudSceneSpec(elementIds = listOf("gauge"), elements = listOf(original))
        )
        val commands = HudCommandStack(document)

        commands.execute(ReplaceElementSpecCommand("gauge", intermediate, HudPath.of("layout")))
        commands.execute(
            ReplaceElementSpecCommand("gauge", final, HudPath.of("layout")),
            mergeWithPrevious = true,
        )

        assertEquals(final, document.elements.single().spec)
        assertTrue(commands.undo())
        assertEquals(original, document.elements.single().spec)
        assertFalse(commands.canUndo)
        assertTrue(commands.redo())
        assertEquals(final, document.elements.single().spec)
    }

    @Test
    fun `different merge keys remain separate undo entries`() {
        val original = GradientGaugeBar.Spec(width = 100)
        val first = GradientGaugeBar.Spec(width = 200)
        val second = GradientGaugeBar.Spec(width = 300)
        val document = HudSceneDocument.from(
            HudSceneSpec(elementIds = listOf("gauge"), elements = listOf(original))
        )
        val commands = HudCommandStack(document)

        commands.execute(ReplaceElementSpecCommand("gauge", first, HudPath.of("layout")))
        commands.execute(
            ReplaceElementSpecCommand("gauge", second, HudPath.of("width")),
            mergeWithPrevious = true,
        )

        assertTrue(commands.undo())
        assertEquals(first, document.elements.single().spec)
        assertTrue(commands.undo())
        assertEquals(original, document.elements.single().spec)
    }

    @Test
    fun `legacy scenes receive deterministic ids`() {
        val document = HudSceneDocument.from(
            HudSceneSpec(elements = listOf(GradientGaugeBar.Spec(), GradientGaugeBar.Spec()))
        )

        assertEquals(listOf("element-1", "element-2"), document.elements.map { it.id })
    }

    @Test
    fun `dirty follows the current content relative to the latest clean snapshot`() {
        val original = GradientGaugeBar.Spec(width = 100)
        val replacement = GradientGaugeBar.Spec(width = 200)
        val document = HudSceneDocument.from(
            HudSceneSpec(elementIds = listOf("gauge"), elements = listOf(original))
        )
        val commands = HudCommandStack(document)

        assertFalse(document.dirty)
        commands.execute(ReplaceElementSpecCommand("gauge", replacement))
        assertTrue(document.dirty)
        commands.undo()
        assertFalse(document.dirty)
        commands.redo()
        assertTrue(document.dirty)

        document.markClean()
        assertFalse(document.dirty)
        commands.undo()
        assertTrue(document.dirty)
        commands.redo()
        assertFalse(document.dirty)
    }

    @Test
    fun `execute undo and redo publish forward and inverse document changes`() {
        val original = GradientGaugeBar.Spec(width = 100)
        val replacement = GradientGaugeBar.Spec(width = 200)
        val document = HudSceneDocument.from(
            HudSceneSpec(elementIds = listOf("gauge"), elements = listOf(original))
        )
        val commands = HudCommandStack(document)
        val changes = mutableListOf<HudDocumentChange>()
        commands.addChangeListener(changes::add)

        commands.execute(ReplaceElementSpecCommand("gauge", replacement))
        commands.undo()
        commands.redo()

        assertEquals(
            listOf(
                HudDocumentChange.SpecReplaced("gauge", original, replacement),
                HudDocumentChange.SpecReplaced("gauge", replacement, original),
                HudDocumentChange.SpecReplaced("gauge", original, replacement),
            ),
            changes,
        )
    }

    @Test
    fun `every structural command publishes its actual index change`() {
        val first = HudDocumentElement("first", GradientGaugeBar.Spec(width = 100))
        val second = HudDocumentElement("second", GradientGaugeBar.Spec(width = 200))
        val added = HudDocumentElement("added", GradientGaugeBar.Spec(width = 300))
        val document = HudSceneDocument.from(
            HudSceneSpec(
                elementIds = listOf(first.id, second.id),
                elements = listOf(first.spec, second.spec),
            )
        )
        val commands = HudCommandStack(document)
        val changes = mutableListOf<HudDocumentChange>()
        commands.addChangeListener(changes::add)

        commands.execute(AddElementCommand(added, 1))
        commands.execute(MoveElementCommand("second", 0))
        commands.execute(RemoveElementCommand("first"))

        assertEquals(
            listOf(
                HudDocumentChange.Added(added, 1),
                HudDocumentChange.Moved("second", 2, 0),
                HudDocumentChange.Removed(first, 1),
            ),
            changes,
        )

        changes.clear()
        commands.undo()
        commands.undo()
        commands.undo()

        assertEquals(
            listOf(
                HudDocumentChange.Added(first, 1),
                HudDocumentChange.Moved("second", 0, 2),
                HudDocumentChange.Removed(added, 1),
            ),
            changes,
        )
        assertEquals(listOf("first", "second"), document.elements.map { it.id })
    }

    @Test
    fun `empty history and closed listener do not publish changes`() {
        val document = HudSceneDocument.from(HudSceneSpec(elements = emptyList()))
        val commands = HudCommandStack(document)
        val changes = mutableListOf<HudDocumentChange>()
        val subscription = commands.addChangeListener(changes::add)

        assertFalse(commands.undo())
        assertFalse(commands.redo())
        subscription.close()
        commands.execute(AddElementCommand(HudDocumentElement("gauge", GradientGaugeBar.Spec())))

        assertTrue(changes.isEmpty())
    }
}
