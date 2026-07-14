package io.github.oni0nfr1.dynamicrider.client.hud.editor.command

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentElement
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSpec
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
    fun `legacy scenes receive deterministic ids`() {
        val document = HudSceneDocument.from(
            HudSceneSpec(elements = listOf(GradientGaugeBar.Spec(), GradientGaugeBar.Spec()))
        )

        assertEquals(listOf("element-1", "element-2"), document.elements.map { it.id })
    }
}
