package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.HeldHudElement
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.layout.HudBounds
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import org.joml.Vector2f
import org.joml.Vector2i
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class HudPreviewElementGuideTest {
    @Test
    fun `hit test prefers z index then later render order`() {
        val guides = listOf(
            guide("back", zIndex = 0f, renderOrder = 2),
            guide("front-first", zIndex = 1f, renderOrder = 0),
            guide("front-last", zIndex = 1f, renderOrder = 1),
        )

        assertEquals(
            "front-last",
            HudPreviewElementGuideCalculator.hitTest(guides, 5f, 5f)?.elementId,
        )
        assertNull(HudPreviewElementGuideCalculator.hitTest(guides, 11f, 5f))
    }

    @Test
    fun `hit test without selection prefers shallower hierarchy depth`() {
        val guides = listOf(
            guide("root", zIndex = 0f, renderOrder = 0),
            guide("root.child", zIndex = 100f, renderOrder = 100),
        )

        assertEquals(
            HudPath.of("root"),
            HudPreviewElementGuideCalculator.hitTest(guides, 5f, 5f)?.path,
        )
    }

    @Test
    fun `scale handle uses the corner opposite to element anchor`() {
        val guide = guide("selected", zIndex = 0f, renderOrder = 0).copy(elementAnchor = HudAnchor.TOP_LEFT)

        assertEquals(10f to 10f, guide.scaleHandle())
    }

    @Test
    fun `scale handle prefers larger coordinates for centered anchor axes`() {
        val centered = guide("centered", zIndex = 0f, renderOrder = 0).copy(elementAnchor = HudAnchor.MIDDLE_CENTER)
        val bottomCenter = centered.copy(elementAnchor = HudAnchor.BOTTOM_CENTER)
        val middleRight = centered.copy(elementAnchor = HudAnchor.MIDDLE_RIGHT)

        assertEquals(10f to 10f, centered.scaleHandle())
        assertEquals(10f to 0f, bottomCenter.scaleHandle())
        assertEquals(0f to 10f, middleRight.scaleHandle())
    }

    @Test
    fun `scale handle direction stays stable when coordinates move during scaling`() {
        val initial = guide("stable", zIndex = 0f, renderOrder = 0).copy(
            bounds = HudBounds(0f, 0f, 10f, 10f),
            elementAnchor = HudAnchor.MIDDLE_CENTER,
        )
        val scaled = initial.copy(
            bounds = HudBounds(-99.999f, -100.001f, 100.001f, 99.999f),
            elementAnchorX = 0.001f,
            elementAnchorY = -0.001f,
        )

        assertEquals(10f to 10f, initial.scaleHandle())
        assertEquals(100.001f to 99.999f, scaled.scaleHandle())
    }

    @Test
    fun `selected descendants unrelated elements self and ancestors form stable priority groups`() {
        val guides = listOf(
            guide("root", zIndex = 100f, renderOrder = 4),
            guide("root.child", zIndex = 0f, renderOrder = 0),
            guide("root.child.grandchild", zIndex = 10f, renderOrder = 3),
            guide("other", zIndex = 5f, renderOrder = 2),
        )

        assertEquals(
            HudPath.parse("root.child"),
            HudPreviewElementGuideCalculator.hitTest(guides, 5f, 5f, HudPath.of("root"))?.path,
        )
        assertEquals(
            HudPath.parse("root.child.grandchild"),
            HudPreviewElementGuideCalculator.hitTest(guides, 5f, 5f, HudPath.parse("root.child"))?.path,
        )
        assertEquals(
            HudPath.of("other"),
            HudPreviewElementGuideCalculator.hitTest(
                guides.filter { it.path == HudPath.of("root") || it.path == HudPath.of("other") },
                5f,
                5f,
                HudPath.parse("root.child"),
            )?.path,
        )
        assertEquals(
            HudPath.parse("root.child"),
            HudPreviewElementGuideCalculator.hitTest(
                guides.filter { it.path == HudPath.of("root") || it.path == HudPath.parse("root.child") },
                5f,
                5f,
                HudPath.parse("root.child"),
            )?.path,
        )
        assertEquals(
            HudPath.of("root"),
            HudPreviewElementGuideCalculator.hitTest(
                guides.filter { it.path == HudPath.of("root") },
                5f,
                5f,
                HudPath.parse("root.child"),
            )?.path,
        )
    }

    @Test
    fun `tree distance crosses a virtual scene root`() {
        assertEquals(1, HudPreviewElementGuideCalculator.treeDistance(HudPath.of("root"), HudPath.parse("root.child")))
        assertEquals(2, HudPreviewElementGuideCalculator.treeDistance(HudPath.parse("root.left"), HudPath.parse("root.right")))
        assertEquals(3, HudPreviewElementGuideCalculator.treeDistance(HudPath.parse("root.child"), HudPath.of("other")))
    }

    @Test
    fun `recursive guides compose parent translation and scale`() {
        val child = FakeElement(width = 10, height = 10).apply {
            position = Vector2i(5, 4)
        }
        val parent = FakeElement(width = 20, height = 20).apply {
            position = Vector2i(10, 12)
            scale = Vector2f(2f)
            children = listOf(HeldHudElement("child", child))
        }
        val root = FakeHolder(100, 100, listOf(HeldHudElement("root", parent)))

        val guides = HudPreviewElementGuideCalculator.calculate(root)
        val parentGuide = guides.single { it.path == HudPath.of("root") }
        val childGuide = guides.single { it.path == HudPath.parse("root.child") }

        assertEquals(HudBounds(10f, 12f, 50f, 52f), parentGuide.bounds)
        assertEquals(HudBounds(20f, 20f, 40f, 40f), childGuide.bounds)
        assertEquals(2f, childGuide.parentTransform.scaleX)
        assertEquals(5f to 4f, childGuide.parentTransform.toLocal(20f, 20f))
    }

    private fun guide(id: String, zIndex: Float, renderOrder: Int) = HudPreviewElementGuide(
        path = HudPath.parse(id),
        element = FakeElement(),
        bounds = HudBounds(0f, 0f, 10f, 10f),
        screenAnchorX = 0f,
        screenAnchorY = 0f,
        elementAnchorX = 0f,
        elementAnchorY = 0f,
        localScreenAnchorX = 0f,
        localScreenAnchorY = 0f,
        elementAnchor = HudAnchor.TOP_LEFT,
        scaleX = 1f,
        scaleY = 1f,
        zIndex = zIndex,
        renderOrder = renderOrder,
        parentTransform = HudPreviewParentTransform.IDENTITY,
    )

    private class FakeElement(
        override val width: Int = 10,
        override val height: Int = 10,
    ) : HudElement<KartState>, ElementHolder {
        override var screenAnchor = HudAnchor.TOP_LEFT
        override var elementAnchor = HudAnchor.TOP_LEFT
        override var scale = Vector2f(1f)
        override var position = Vector2i()
        override var zIndex = 0f
        var children: List<HeldHudElement> = emptyList()
        override val heldElements: List<HeldHudElement> get() = children
        override fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) = Unit
    }

    private data class FakeHolder(
        override val width: Int,
        override val height: Int,
        override val heldElements: List<HeldHudElement>,
    ) : ElementHolder
}
