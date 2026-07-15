package io.github.oni0nfr1.dynamicrider.client.hud.editor.session

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudElementInspectionResult
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.PlainNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.V1Tachometer
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneRepository
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneCodec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudScenePaths
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneResourceSource
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSource
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.SerializationException
import net.minecraft.resources.ResourceLocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.nio.file.Files

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
        val inspected = assertInstanceOf(
            HudElementInspectionResult.Inspected::class.java,
            session.inspectElement("slot"),
        )
        assertEquals(
            64,
            inspected.model.properties.single { it.path == HudPropertyPath.of("iconSize") }
                .value.jsonPrimitive.content.toInt(),
        )
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

        val palette = session.availableElementTypes()
        assertTrue(palette.any { it.typeId == HudElementTypeRegistry.PLAIN_NITRO_SLOT.id })
        assertTrue(palette.none { it.typeId == HudElementTypeRegistry.V1_TACHOMETER.id })

        val added = session.addElement(HudElementTypeRegistry.PLAIN_NITRO_SLOT.id)

        assertEquals(HudEditorActionResult.Applied("element-1"), added)
        assertEquals("element-1", session.state.selectedElementId)
        assertEquals(listOf("slot", "element-1"), session.state.elements.map { it.id })

        assertEquals(HudEditorActionResult.Applied("element-1"), session.moveElement("element-1", 0))
        assertEquals(listOf("element-1", "slot"), session.state.elements.map { it.id })

        assertEquals(HudEditorActionResult.Applied("element-1"), session.removeElement("element-1"))
        assertEquals(listOf("slot"), session.state.elements.map { it.id })
        assertEquals("slot", session.state.selectedElementId)
        assertEquals(session.state.elements.map { it.id }, session.previewScene.entries.map { it.id })
        assertEquals(
            HudEditorActionResult.ElementTypeNotFound("missing-type"),
            session.addElement("missing-type"),
        )
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

    @Test
    fun `save writes current document and moves the clean snapshot without replacing preview`() {
        val resourceSpec = HudSceneSpec(
            elementIds = listOf("slot"),
            elements = listOf(PlainNitroSlot.Spec(iconSize = 32)),
        )
        val resources = FakeHudSceneResourceSource(
            mapOf(HudScenePaths.resourceHudSceneId(HudSceneMode.RIDE, KartStateTypes.JIU) to resourceSpec)
        )
        val session = openSession(HudSceneRepository(root, resources))
        val previewScene = session.previewScene
        assertEquals(HudSceneSource.RESOURCE, session.state.source)
        session.updateProperty("slot", HudPropertyPath.of("iconSize"), JsonPrimitive(64))

        val result = assertInstanceOf(HudEditorPersistenceResult.Saved::class.java, session.save())

        assertEquals(
            64,
            (HudSceneCodec.decode(Files.readString(result.path)).elements.single() as PlainNitroSlot.Spec).iconSize,
        )
        assertSame(previewScene, session.previewScene)
        assertEquals(HudSceneSource.CUSTOM_CONFIG, session.state.source)
        assertFalse(session.state.dirty)
        assertTrue(session.state.canUndo)

        session.undo()
        assertTrue(session.state.dirty)
    }

    @Test
    fun `delete requires confirmation then restores resource document history and preview`() {
        val resourceSpec = HudSceneSpec(
            elementIds = listOf("resource-slot"),
            elements = listOf(PlainNitroSlot.Spec(iconSize = 48)),
        )
        val resources = FakeHudSceneResourceSource(
            mapOf(HudScenePaths.resourceHudSceneId(HudSceneMode.RIDE, KartStateTypes.JIU) to resourceSpec)
        )
        val repository = HudSceneRepository(root, resources)
        val session = session(repository)
        val previewScene = session.previewScene
        session.updateProperty("slot", HudPropertyPath.of("iconSize"), JsonPrimitive(64))

        assertSame(HudEditorPersistenceResult.DiscardConfirmationRequired, session.deleteCustom())
        assertTrue(Files.exists(HudScenePaths.customHudScenePath(root, HudSceneMode.RIDE, KartStateTypes.JIU)))

        val restored = assertInstanceOf(
            HudEditorPersistenceResult.Restored::class.java,
            session.deleteCustom(discardUnsavedChanges = true),
        )

        assertTrue(restored.customDeleted)
        assertEquals(HudSceneSource.RESOURCE, restored.source)
        assertEquals(HudSceneSource.RESOURCE, session.state.source)
        assertEquals(listOf("resource-slot"), session.state.elements.map { it.id })
        assertEquals(48, (session.state.elements.single().spec as PlainNitroSlot.Spec).iconSize)
        assertEquals(session.state.elements.map { it.id }, previewScene.entries.map { it.id })
        assertSame(previewScene, session.previewScene)
        assertFalse(session.state.dirty)
        assertFalse(session.state.canUndo)
        assertFalse(session.state.canRedo)
    }

    private fun session(
        repository: HudSceneRepository = HudSceneRepository(root),
    ): HudEditorSession<out KartState> {
        repository.saveCustom(
            HudSceneMode.RIDE,
            KartStateTypes.JIU,
            HudSceneSpec(
                elementIds = listOf("slot"),
                elements = listOf(PlainNitroSlot.Spec(iconSize = 32)),
            ),
        ).getOrThrow()
        return openSession(repository)
    }

    private fun openSession(repository: HudSceneRepository): HudEditorSession<out KartState> {
        val result = HudEditorSessionFactory(repository).open(
            mode = HudSceneMode.RIDE,
            stateType = KartStateTypes.JIU,
            viewport = viewport(),
        )
        val opened = assertInstanceOf(HudEditorSessionOpenResult.Opened::class.java, result)
        return opened.session
    }

    private fun viewport(): ElementHolder = object : ElementHolder {
        override val width: Int = 800
        override val height: Int = 600
    }

    private class FakeHudSceneResourceSource(
        private val scenes: Map<ResourceLocation, HudSceneSpec>,
    ) : HudSceneResourceSource {
        override fun get(id: ResourceLocation): HudSceneSpec? = scenes[id]

        override fun getLoadError(id: ResourceLocation): SerializationException? = null
    }
}
