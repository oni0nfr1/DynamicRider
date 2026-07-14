package io.github.oni0nfr1.dynamicrider.client.hud.editor.service

import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.HudCommandStack
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSpec
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HudSpecEditServiceTest {
    @Test
    fun `valid property change creates a command without immediately changing the document`() {
        val original = GradientGaugeBar.Spec(width = 120)
        val document = document(original)
        val service = HudSpecEditService(document)

        val result = service.createPropertyChangeCommand(
            "gauge",
            HudPropertyPath.parse("width"),
            JsonPrimitive(240),
        )

        val created = assertInstanceOf(HudSpecEditCommandResult.Created::class.java, result)
        assertEquals(original, document.elementById("gauge")?.spec)
        assertFalse(document.dirty)

        val commandStack = HudCommandStack(document)
        commandStack.execute(created.command)
        assertEquals(240, (document.elementById("gauge")?.spec as GradientGaugeBar.Spec).width)
        assertTrue(document.dirty)

        assertTrue(commandStack.undo())
        assertEquals(original, document.elementById("gauge")?.spec)
    }

    @Test
    fun `invalid property change returns rejection without creating undo history`() {
        val original = GradientGaugeBar.Spec(width = 120)
        val document = document(original)
        val service = HudSpecEditService(document)
        val commandStack = HudCommandStack(document)

        val result = service.createPropertyChangeCommand(
            "gauge",
            HudPropertyPath.parse("width"),
            JsonPrimitive(3_000),
        )

        val rejected = assertInstanceOf(HudSpecEditCommandResult.PropertyRejected::class.java, result)
        assertEquals(HudPropertyPath.parse("width"), rejected.failure.path)
        assertEquals(original, document.elementById("gauge")?.spec)
        assertFalse(document.dirty)
        assertFalse(commandStack.canUndo)
    }

    @Test
    fun `unknown element id is returned separately from property rejection`() {
        val document = document(GradientGaugeBar.Spec())

        val result = HudSpecEditService(document).createPropertyChangeCommand(
            "missing",
            HudPropertyPath.parse("width"),
            JsonPrimitive(240),
        )

        assertEquals(HudSpecEditCommandResult.ElementNotFound("missing"), result)
        assertFalse(document.dirty)
    }

    @Test
    fun `equal property value does not create a command`() {
        val document = document(GradientGaugeBar.Spec(width = 120))

        val result = HudSpecEditService(document).createPropertyChangeCommand(
            "gauge",
            HudPropertyPath.parse("width"),
            JsonPrimitive(120),
        )

        assertEquals(HudSpecEditCommandResult.Unchanged("gauge"), result)
        assertFalse(document.dirty)
    }

    private fun document(spec: GradientGaugeBar.Spec): HudSceneDocument = HudSceneDocument.from(
        HudSceneSpec(
            elementIds = listOf("gauge"),
            elements = listOf(spec),
        )
    )
}
