package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudElementInspectionResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditablePropertySchema
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudElementInspector
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class HudLayoutEditorModelTest {
    @Test
    fun `layout object exposes metadata fields and calculates multi-leaf replacements`() {
        val spec = GradientGaugeBar.Spec(
            layout = io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec(
                elementAnchor = io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor.MIDDLE_CENTER,
                x = 10,
                y = 20,
            ),
        )
        val document = HudSceneDocument.from(
            HudSceneSpec(elementIds = listOf("gauge"), elements = listOf(spec)),
        )
        val model = (HudElementInspector(document).inspect("gauge") as HudElementInspectionResult.Inspected).model
        val layout = model.properties.single { it.path.toString() == "layout" }
        val fields = (layout.schema as HudEditablePropertySchema.Object).properties
        val x = fields.single { it.path.toString() == "layout.x" }
        val screenAnchor = fields.single { it.path.toString() == "layout.screenAnchor" }

        assertEquals(7, fields.size)
        assertInstanceOf(HudPropertyEditorType.NumberInput::class.java, x.editor)
        assertInstanceOf(HudPropertyEditorType.AnchorSelector::class.java, screenAnchor.editor)

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
