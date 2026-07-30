package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditableProperty
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditablePropertySchema
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class HudLayoutEditorModelTest {
    @Test
    fun `layout fields replace one value while preserving the complete object`() {
        val layout = HudEditableProperty(
            path = HudPropertyPath.of("layout"),
            nameKey = "layout",
            descriptionKey = null,
            schema = HudEditablePropertySchema.Leaf(HudPropertyEditorType.LayoutEditor),
            value = buildJsonObject {
                put("screenAnchor", JsonPrimitive("TOP_LEFT"))
                put("elementAnchor", JsonPrimitive("MIDDLE_CENTER"))
                put("scaleX", JsonPrimitive(1f))
                put("scaleY", JsonPrimitive(1f))
                put("x", JsonPrimitive(10))
                put("y", JsonPrimitive(20))
                put("zIndex", JsonPrimitive(0f))
            },
            optional = true,
            nullable = false,
        )

        val fields = HudLayoutEditorModel.fields(layout)
        val x = fields.single { it.path == HudPropertyPath.of("layout", "x") }
        val screenAnchor = fields.single { it.path == HudPropertyPath.of("layout", "screenAnchor") }
        val replacement = HudLayoutEditorModel.replace(layout, x, JsonPrimitive(64))

        assertEquals(7, fields.size)
        assertInstanceOf(HudPropertyEditorType.NumberInput::class.java, x.editor)
        assertInstanceOf(HudPropertyEditorType.AnchorSelector::class.java, screenAnchor.editor)
        assertEquals(JsonPrimitive(64), replacement["x"])
        assertEquals(JsonPrimitive(20), replacement["y"])
        assertEquals(JsonPrimitive("MIDDLE_CENTER"), replacement["elementAnchor"])

        val moved = HudLayoutEditorModel.replacePosition(layout, -12, 48)
        assertEquals(JsonPrimitive(-12), moved["x"])
        assertEquals(JsonPrimitive(48), moved["y"])
        assertEquals(JsonPrimitive(1f), moved["scaleX"])

        val translated = HudLayoutEditorModel.translatePosition(layout, -1, 10)
        assertEquals(JsonPrimitive(9), translated["x"])
        assertEquals(JsonPrimitive(30), translated["y"])
        assertEquals(JsonPrimitive("MIDDLE_CENTER"), translated["elementAnchor"])

        val scaled = HudLayoutEditorModel.replaceUniformScale(layout, 1.75f)
        assertEquals(JsonPrimitive(1.75f), scaled["scaleX"])
        assertEquals(JsonPrimitive(1.75f), scaled["scaleY"])
        assertEquals(JsonPrimitive(10), scaled["x"])
    }
}
