package io.github.oni0nfr1.dynamicrider.client.hud.editor.property

import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Failure
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Reason
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Success
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu.JiuTachometer
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Test

class HudSpecPropertyEditorTest {
    @Test
    fun `primitive update creates a new spec without mutating the original`() {
        val original = GradientGaugeBar.Spec()

        val result = HudSpecPropertyEditor.update(original, HudPropertyPath.parse("width"), JsonPrimitive(240))

        val updated = assertInstanceOf(Success::class.java, result).spec as GradientGaugeBar.Spec
        assertNotSame(original, updated)
        assertEquals(120, original.width)
        assertEquals(240, updated.width)
    }

    @Test
    fun `layout child update preserves the rest of the layout`() {
        val original = GradientGaugeBar.Spec()

        val result = HudSpecPropertyEditor.update(original, HudPropertyPath.parse("layout.x"), JsonPrimitive(25))

        val updated = assertInstanceOf(Success::class.java, result).spec as GradientGaugeBar.Spec
        assertEquals(original.layout.copy(x = 25), updated.layout)
        assertEquals(0, original.layout.x)
    }

    @Test
    fun `range violation returns the edited property path`() {
        val path = HudPropertyPath.parse("width")

        val result = HudSpecPropertyEditor.update(GradientGaugeBar.Spec(), path, JsonPrimitive(3_000))

        val failure = assertInstanceOf(Failure::class.java, result)
        assertEquals(path, failure.path)
        assertEquals(Reason.INVALID_VALUE, failure.reason)
    }

    @Test
    fun `serializer type violation is returned instead of thrown`() {
        val result = HudSpecPropertyEditor.update(
            GradientGaugeBar.Spec(),
            HudPropertyPath.parse("width"),
            JsonPrimitive("wide"),
        )

        assertEquals(Reason.INVALID_VALUE, assertInstanceOf(Failure::class.java, result).reason)
    }

    @Test
    fun `unknown property is distinguished from an invalid value`() {
        val result = HudSpecPropertyEditor.update(
            GradientGaugeBar.Spec(),
            HudPropertyPath.parse("missing"),
            JsonPrimitive(1),
        )

        assertEquals(Reason.UNKNOWN_PROPERTY, assertInstanceOf(Failure::class.java, result).reason)
    }

    @Test
    fun `list and nested element specs stay unsupported`() {
        val listResult = HudSpecPropertyEditor.update(
            GradientGaugeBar.Spec(),
            HudPropertyPath.parse("gradientStops"),
            JsonPrimitive("not-a-list"),
        )
        val nestedSpecResult = HudSpecPropertyEditor.update(
            JiuTachometer.Spec(),
            HudPropertyPath.parse("speedometer"),
            JsonPrimitive("not-a-spec"),
        )

        assertEquals(Reason.UNSUPPORTED_PROPERTY, assertInstanceOf(Failure::class.java, listResult).reason)
        assertEquals(Reason.UNSUPPORTED_PROPERTY, assertInstanceOf(Failure::class.java, nestedSpecResult).reason)
    }
}
