package io.github.oni0nfr1.dynamicrider.client.hud.editor.property

import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Failure
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Reason
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Success
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.debug.EditorPropertyStressElement
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

    @Test
    fun `sealed subtype leaf can be updated through its complete path`() {
        val original = EditorPropertyStressElement.Spec()

        val result = HudSpecPropertyEditor.update(
            original,
            HudPropertyPath.parse("style.color"),
            JsonPrimitive("#FFAABBCC"),
        )

        val updated = assertInstanceOf(Success::class.java, result).spec as EditorPropertyStressElement.Spec
        assertEquals(0xFFAABBCC.toInt(), (updated.style as EditorPropertyStressElement.StressStyle.Solid).color)
        assertEquals(EditorPropertyStressElement.StressStyle.Solid(), original.style)
    }

    @Test
    fun `sealed subtype can be replaced from defaults and rejects unknown variants`() {
        val original = EditorPropertyStressElement.Spec()
        val path = HudPropertyPath.parse("style")

        val changed = HudSpecPropertyEditor.changeVariant(original, path, "outline")
        val updated = assertInstanceOf(Success::class.java, changed).spec as EditorPropertyStressElement.Spec
        assertEquals(EditorPropertyStressElement.StressStyle.Outline(), updated.style)

        val rejected = HudSpecPropertyEditor.changeVariant(original, path, "missing")
        assertEquals(Reason.INVALID_VALUE, assertInstanceOf(Failure::class.java, rejected).reason)
    }

    @Test
    fun `selecting the current sealed subtype preserves its configured values`() {
        val original = EditorPropertyStressElement.Spec(
            style = EditorPropertyStressElement.StressStyle.Outline(thickness = 3)
        )

        val result = HudSpecPropertyEditor.changeVariant(
            original,
            HudPropertyPath.parse("style"),
            "outline",
        )

        assertEquals(original, assertInstanceOf(Success::class.java, result).spec)
    }

    @Test
    fun `sealed subtype leaf uses the common validator`() {
        val original = EditorPropertyStressElement.Spec(
            style = EditorPropertyStressElement.StressStyle.Outline()
        )
        val path = HudPropertyPath.parse("style.thickness")

        val result = HudSpecPropertyEditor.update(original, path, JsonPrimitive(10))

        val failure = assertInstanceOf(Failure::class.java, result)
        assertEquals(path, failure.path)
        assertEquals(Reason.INVALID_VALUE, failure.reason)
    }
}
