package io.github.oni0nfr1.dynamicrider.client.hud.editor.session

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.DefaultPreviewJiuKartState
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.V1Tachometer
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneElementFactory
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSource
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.JiuKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import org.joml.Vector2f
import org.joml.Vector2i
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HudEditorSessionTest {
    @Test
    fun `property edits undo and redo update state and one preview scene`() {
        val session = session()
        val scene = session.previewScene
        val states = mutableListOf<HudEditorState>()
        session.addStateListener(listener = states::add)

        val result = session.updateProperty(
            "gauge",
            HudPropertyPath.parse("width"),
            JsonPrimitive(240),
        )

        assertEquals(HudEditorActionResult.Applied("gauge"), result)
        assertSame(scene, session.previewScene)
        assertEquals(240, (session.state.elements.single().spec as GradientGaugeBar.Spec).width)
        assertEquals(240, (scene.entries.single().spec as GradientGaugeBar.Spec).width)
        assertTrue(session.state.dirty)
        assertTrue(session.state.canUndo)
        assertFalse(session.state.canRedo)

        assertInstanceOf(HudEditorActionResult.Applied::class.java, session.undo())
        assertEquals(100, (session.state.elements.single().spec as GradientGaugeBar.Spec).width)
        assertFalse(session.state.dirty)
        assertTrue(session.state.canRedo)

        session.redo()
        assertEquals(240, (session.state.elements.single().spec as GradientGaugeBar.Spec).width)
        assertTrue(states.size >= 4)
        assertEquals(session.state, states.last())
    }

    @Test
    fun `session owns element ids selection and structural commands`() {
        val session = session()
        assertEquals(HudEditorActionResult.Applied("gauge"), session.selectElement("gauge"))

        val added = session.addElement(GradientGaugeBar.Spec(width = 200))

        assertEquals(HudEditorActionResult.Applied("element-1"), added)
        assertEquals("element-1", session.state.selectedElementId)
        assertEquals(listOf("gauge", "element-1"), session.state.elements.map { it.id })

        assertEquals(HudEditorActionResult.Applied("element-1"), session.moveElement("element-1", 0))
        assertEquals(listOf("element-1", "gauge"), session.state.elements.map { it.id })

        assertEquals(HudEditorActionResult.Applied("element-1"), session.removeElement("element-1"))
        assertEquals(listOf("gauge"), session.state.elements.map { it.id })
        assertEquals("gauge", session.state.selectedElementId)
        assertEquals(session.state.elements.map { it.id }, session.previewScene.entries.map { it.id })
    }

    @Test
    fun `rejected changes leave document history and preview unchanged`() {
        val session = session()

        val invalid = session.addElement(GradientGaugeBar.Spec(width = 3_000))
        val incompatible = session.addElement(V1Tachometer.Spec())
        val rejectedProperty = session.updateProperty(
            "gauge",
            HudPropertyPath.parse("width"),
            JsonPrimitive(3_000),
        )
        val missing = session.updateProperty(
            "missing",
            HudPropertyPath.parse("width"),
            JsonPrimitive(200),
        )

        assertInstanceOf(HudEditorActionResult.InvalidSpec::class.java, invalid)
        assertInstanceOf(HudEditorActionResult.IncompatibleState::class.java, incompatible)
        assertInstanceOf(HudEditorActionResult.PropertyRejected::class.java, rejectedProperty)
        assertEquals(HudEditorActionResult.ElementNotFound("missing"), missing)
        assertEquals(listOf("gauge"), session.state.elements.map { it.id })
        assertEquals(listOf("gauge"), session.previewScene.entries.map { it.id })
        assertFalse(session.state.dirty)
        assertFalse(session.state.canUndo)
    }

    @Test
    fun `closing publishes final state and rejects later edits`() {
        val session = session()
        val states = mutableListOf<HudEditorState>()
        session.addStateListener(listener = states::add)

        session.close()

        assertTrue(session.state.closed)
        assertFalse(session.previewScene.isActive)
        assertTrue(states.last().closed)
        assertSame(HudEditorActionResult.Closed, session.undo())
        assertSame(HudEditorActionResult.Closed, session.selectElement("gauge"))
    }

    @Test
    fun `factory can create the preview context from a state type`() {
        val viewport = object : ElementHolder {
            override val width: Int = 800
            override val height: Int = 600
        }

        val session = HudEditorSessionFactory.create(
            mode = HudSceneMode.RIDE,
            source = HudSceneSource.RESOURCE,
            spec = HudSceneSpec(elements = emptyList()),
            stateType = KartStateTypes.JIU,
            viewport = viewport,
        ).getOrThrow()

        assertEquals(KartStateTypes.JIU, session.state.kartStateType)
        assertTrue(session.previewScene.isActive)
        session.close()
    }

    private fun session(): HudEditorSession<JiuKartState> {
        val context = PreviewHudSceneContext(KartStateTypes.JIU, DefaultPreviewJiuKartState())
        val viewport = object : ElementHolder {
            override val width: Int = 800
            override val height: Int = 600
        }
        return HudEditorSessionFactory.create(
            mode = HudSceneMode.RIDE,
            source = HudSceneSource.RESOURCE,
            spec = HudSceneSpec(
                elementIds = listOf("gauge"),
                elements = listOf(GradientGaugeBar.Spec(width = 100)),
            ),
            previewContext = context,
            viewport = viewport,
            elementFactory = HudSceneElementFactory { spec, _, _ -> FakeHudElement(spec) },
        ).getOrThrow()
    }

    private class FakeHudElement(
        val spec: HudElementSpec<*, JiuKartState>,
    ) : HudElement<JiuKartState> {
        override var screenAnchor: HudAnchor = HudAnchor.TOP_LEFT
        override var elementAnchor: HudAnchor = HudAnchor.TOP_LEFT
        override var scale: Vector2f = Vector2f(1f)
        override var position: Vector2i = Vector2i()
        override var zIndex: Float = 0f

        override fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) = Unit
    }
}
