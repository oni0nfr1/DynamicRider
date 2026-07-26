package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.AddElementCommand
import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.HudCommandStack
import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.MoveElementCommand
import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.ReplaceElementSpecCommand
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentElement
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneElementFactory
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.JiuKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import org.joml.Vector2f
import org.joml.Vector2i
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PreviewHudSceneSynchronizerTest {
    @Test
    fun `initial snapshot and document changes update one active scene instance`() {
        val first = GradientGaugeBar.Spec(width = 100)
        val second = GradientGaugeBar.Spec(width = 200)
        val document = document(first, second)
        val commands = HudCommandStack(document)
        val scene = scene()
        val synchronizer = PreviewHudSceneSynchronizer(document, commands, scene)

        assertTrue(scene.isActive)
        assertEquals(listOf("first", "second"), scene.entries.map { it.id })
        assertTrue(scene.entries.all { it.element != null })
        val originalFirstElement = scene.entries.first().element

        commands.execute(ReplaceElementSpecCommand("first", GradientGaugeBar.Spec(width = 300)))
        assertSame(scene, synchronizer.scene)
        assertEquals(300, (scene.entries.first().spec as GradientGaugeBar.Spec).width)
        assertNotSame(originalFirstElement, scene.entries.first().element)

        val added = HudDocumentElement("added", GradientGaugeBar.Spec(width = 400))
        commands.execute(AddElementCommand(added, 1))
        commands.execute(MoveElementCommand("second", 0))
        assertEquals(listOf("second", "first", "added"), scene.entries.map { it.id })

        commands.undo()
        commands.undo()
        assertEquals(listOf("first", "second"), scene.entries.map { it.id })
        assertNull(synchronizer.lastFailure)
    }

    @Test
    fun `incremental drift is recovered from the current document snapshot`() {
        val document = document(GradientGaugeBar.Spec(width = 100))
        val commands = HudCommandStack(document)
        val scene = scene()
        val synchronizer = PreviewHudSceneSynchronizer(document, commands, scene)
        scene.removeElement("first")

        commands.execute(ReplaceElementSpecCommand("first", GradientGaugeBar.Spec(width = 250)))

        assertEquals(listOf("first"), scene.entries.map { it.id })
        assertEquals(250, (scene.entries.single().spec as GradientGaugeBar.Spec).width)
        assertNull(synchronizer.lastFailure)
    }

    @Test
    fun `closing stops synchronization and deactivates the preview scene`() {
        val document = document(GradientGaugeBar.Spec(width = 100))
        val commands = HudCommandStack(document)
        val scene = scene()
        val synchronizer = PreviewHudSceneSynchronizer(document, commands, scene)

        synchronizer.close()
        commands.execute(ReplaceElementSpecCommand("first", GradientGaugeBar.Spec(width = 300)))

        assertFalse(scene.isActive)
        assertEquals(100, (scene.entries.single().spec as GradientGaugeBar.Spec).width)
        assertTrue(scene.entries.all { it.element == null })
    }

    private fun document(vararg specs: GradientGaugeBar.Spec): HudSceneDocument = HudSceneDocument.from(
        HudSceneSpec(
            elementIds = specs.indices.map { if (it == 0) "first" else "second" },
            elements = specs.toList(),
        )
    )

    private fun scene(): HudScene<JiuKartState> {
        val context = PreviewHudSceneContextFactory.create(KartStateTypes.JIU)
        val viewport = object : ElementHolder {
            override val width: Int = 800
            override val height: Int = 600
        }
        return HudScene(
            context,
            viewport,
            HudSceneElementFactory { spec, _, _ -> FakeHudElement(spec) },
        )
    }

    private class FakeHudElement(
        val spec: HudElementSpec<*, JiuKartState>,
    ) : HudElement<JiuKartState> {
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
