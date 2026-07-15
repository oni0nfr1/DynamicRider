package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.layout.HudBounds
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

    private fun guide(id: String, zIndex: Float, renderOrder: Int) = HudPreviewElementGuide(
        elementId = id,
        bounds = HudBounds(0f, 0f, 10f, 10f),
        screenAnchorX = 0f,
        screenAnchorY = 0f,
        elementAnchorX = 0f,
        elementAnchorY = 0f,
        elementAnchor = HudAnchor.TOP_LEFT,
        scaleX = 1f,
        scaleY = 1f,
        zIndex = zIndex,
        renderOrder = renderOrder,
    )
}
