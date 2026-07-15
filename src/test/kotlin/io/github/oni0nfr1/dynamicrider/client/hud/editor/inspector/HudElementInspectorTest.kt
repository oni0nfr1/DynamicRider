package io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HudElementInspectorTest {
    @Test
    fun `inspector combines registry metadata with current serialized values`() {
        val spec = GradientGaugeBar.Spec(width = 180)
        val document = HudSceneDocument.from(
            HudSceneSpec(elementIds = listOf("gauge"), elements = listOf(spec))
        )

        val result = assertInstanceOf(
            HudElementInspectionResult.Inspected::class.java,
            HudElementInspector(document).inspect("gauge"),
        )

        val model = result.model
        assertEquals("gauge", model.elementId)
        assertEquals(HudElementTypeRegistry.GRADIENT_GAUGE_BAR.id, model.typeId)
        val width = model.properties.single { it.path == HudPropertyPath.of("width") }
        assertEquals(180, width.value.jsonPrimitive.content.toInt())
        assertInstanceOf(HudPropertyEditorType.Slider::class.java, width.editor)

        val unsupported = model.properties.single { it.path == HudPropertyPath.of("gradientStops") }
        assertInstanceOf(HudPropertyEditorType.Unsupported::class.java, unsupported.editor)
        assertFalse(unsupported.supported)
        assertEquals(
            HudElementTypeRegistry.GRADIENT_GAUGE_BAR.metadata.properties
                .filterNot { it.hidden }
                .map { it.serialName },
            model.properties.map { it.path.toString() },
        )
        assertTrue(model.properties.none { it.path.segments.any(String::isBlank) })
    }

    @Test
    fun `unknown element id is returned without encoding`() {
        val document = HudSceneDocument.from(HudSceneSpec(elements = emptyList()))

        assertEquals(
            HudElementInspectionResult.ElementNotFound("missing"),
            HudElementInspector(document).inspect("missing"),
        )
    }
}
