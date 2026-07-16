package io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.debug.EditorPropertyStressElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.joml.Vector2f
import org.joml.Vector2i

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

    @Test
    fun `hidden metadata is excluded while unsupported metadata remains visible`() {
        val document = HudSceneDocument.from(
            HudSceneSpec(
                elementIds = listOf("stress", "gauge"),
                elements = listOf(EditorPropertyStressElement.Spec(), GradientGaugeBar.Spec()),
            )
        )
        val inspector = HudElementInspector(document)

        val stress = assertInstanceOf(
            HudElementInspectionResult.Inspected::class.java,
            inspector.inspect("stress"),
        ).model
        val gauge = assertInstanceOf(
            HudElementInspectionResult.Inspected::class.java,
            inspector.inspect("gauge"),
        ).model

        assertTrue(HudElementTypeRegistry.EDITOR_PROPERTY_STRESS_TEST.metadata.properties.single { it.serialName == "value20" }.hidden)
        assertTrue(stress.properties.none { it.path == HudPropertyPath.of("value20") })
        assertFalse(gauge.properties.single { it.path == HudPropertyPath.of("gradientStops") }.supported)
    }

    @Test
    fun `unregistered spec returns a structured inspection error`() {
        val document = HudSceneDocument.from(
            HudSceneSpec(elementIds = listOf("unknown"), elements = listOf(UnregisteredSpec()))
        )

        val result = assertInstanceOf(
            HudElementInspectionResult.UnregisteredSpec::class.java,
            HudElementInspector(document).inspect("unknown"),
        )

        assertEquals("unknown", result.elementId)
        assertEquals(UnregisteredSpec::class.java.name, result.specType)
    }

    private data class UnregisteredSpec(
        override val layout: HudLayoutSpec = HudLayoutSpec(),
    ) : HudElementSpec<UnregisteredElement, KartState> {
        override fun requiredStateClass(): Class<out KartState> = KartState::class.java

        override fun create(context: HudSceneContext<KartState>, parent: ElementHolder) = UnregisteredElement()
    }

    private class UnregisteredElement : HudElement<KartState> {
        override val width: Int = 1
        override val height: Int = 1
        override var screenAnchor: HudAnchor = HudAnchor.TOP_LEFT
        override var elementAnchor: HudAnchor = HudAnchor.TOP_LEFT
        override var scale: Vector2f = Vector2f(1f)
        override var position: Vector2i = Vector2i()
        override var zIndex: Float = 0f

        override fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) = Unit
    }
}
