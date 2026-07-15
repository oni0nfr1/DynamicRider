package io.github.oni0nfr1.dynamicrider.client.hud.editor.session

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.PlainNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.V1Tachometer
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneRepository
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSource
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class HudEditorSessionTest {
    @TempDir
    lateinit var root: Path

    @Test
    fun `property edits undo and redo update state and one preview scene`() {
        val session = session()
        val scene = session.previewScene
        val states = mutableListOf<HudEditorState>()
        session.addStateListener(listener = states::add)

        val result = session.updateProperty(
            "slot",
            HudPropertyPath.parse("iconSize"),
            JsonPrimitive(64),
        )

        assertEquals(HudEditorActionResult.Applied("slot"), result)
        assertSame(scene, session.previewScene)
        assertEquals(64, (session.state.elements.single().spec as PlainNitroSlot.Spec).iconSize)
        assertEquals(64, (scene.entries.single().spec as PlainNitroSlot.Spec).iconSize)
        assertTrue(session.state.dirty)
        assertTrue(session.state.canUndo)
        assertFalse(session.state.canRedo)

        assertInstanceOf(HudEditorActionResult.Applied::class.java, session.undo())
        assertEquals(32, (session.state.elements.single().spec as PlainNitroSlot.Spec).iconSize)
        assertFalse(session.state.dirty)
        assertTrue(session.state.canRedo)

        session.redo()
        assertEquals(64, (session.state.elements.single().spec as PlainNitroSlot.Spec).iconSize)
        assertTrue(states.size >= 4)
        assertEquals(session.state, states.last())
    }

    @Test
    fun `session owns element ids selection and structural commands`() {
        val session = session()
        assertEquals(HudEditorActionResult.Applied("slot"), session.selectElement("slot"))

        val added = session.addElement(PlainNitroSlot.Spec(iconSize = 48))

        assertEquals(HudEditorActionResult.Applied("element-1"), added)
        assertEquals("element-1", session.state.selectedElementId)
        assertEquals(listOf("slot", "element-1"), session.state.elements.map { it.id })

        assertEquals(HudEditorActionResult.Applied("element-1"), session.moveElement("element-1", 0))
        assertEquals(listOf("element-1", "slot"), session.state.elements.map { it.id })

        assertEquals(HudEditorActionResult.Applied("element-1"), session.removeElement("element-1"))
        assertEquals(listOf("slot"), session.state.elements.map { it.id })
        assertEquals("slot", session.state.selectedElementId)
        assertEquals(session.state.elements.map { it.id }, session.previewScene.entries.map { it.id })
    }

    @Test
    fun `rejected changes leave document history and preview unchanged`() {
        val session = session()

        val invalid = session.addElement(PlainNitroSlot.Spec(iconSize = 3_000))
        val incompatible = session.addElement(V1Tachometer.Spec())
        val rejectedProperty = session.updateProperty(
            "slot",
            HudPropertyPath.parse("iconSize"),
            JsonPrimitive(3_000),
        )
        val missing = session.updateProperty(
            "missing",
            HudPropertyPath.parse("iconSize"),
            JsonPrimitive(64),
        )

        assertInstanceOf(HudEditorActionResult.InvalidSpec::class.java, invalid)
        assertInstanceOf(HudEditorActionResult.IncompatibleState::class.java, incompatible)
        assertInstanceOf(HudEditorActionResult.PropertyRejected::class.java, rejectedProperty)
        assertEquals(HudEditorActionResult.ElementNotFound("missing"), missing)
        assertEquals(listOf("slot"), session.state.elements.map { it.id })
        assertEquals(listOf("slot"), session.previewScene.entries.map { it.id })
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
        assertSame(HudEditorActionResult.Closed, session.selectElement("slot"))
    }

    @Test
    fun `open distinguishes repository resolution failure`() {
        val result = HudEditorSessionFactory(HudSceneRepository(root)).open(
            mode = HudSceneMode.RIDE,
            stateType = KartStateTypes.JIU,
            viewport = viewport(),
        )

        assertInstanceOf(HudEditorSessionOpenResult.ResolveFailed::class.java, result)
    }

    private fun session(): HudEditorSession<out KartState> {
        val repository = HudSceneRepository(root)
        repository.saveCustom(
            HudSceneMode.RIDE,
            KartStateTypes.JIU,
            HudSceneSpec(
                elementIds = listOf("slot"),
                elements = listOf(PlainNitroSlot.Spec(iconSize = 32)),
            ),
        ).getOrThrow()
        val result = HudEditorSessionFactory(repository).open(
            mode = HudSceneMode.RIDE,
            stateType = KartStateTypes.JIU,
            viewport = viewport(),
        )
        val opened = assertInstanceOf(HudEditorSessionOpenResult.Opened::class.java, result)
        assertEquals(HudSceneSource.CUSTOM_CONFIG, opened.session.source)
        return opened.session
    }

    private fun viewport(): ElementHolder = object : ElementHolder {
        override val width: Int = 800
        override val height: Int = 600
    }
}
