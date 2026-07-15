package io.github.oni0nfr1.dynamicrider.client.hud.layout

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HudLayoutEngineTest {
    @Test
    fun `anchors and scale produce screen bounds and inverse coordinates`() {
        val result = HudLayoutEngine.resolve(
            layout = HudLayoutSpec(
                screenAnchor = HudAnchor.MIDDLE_CENTER,
                elementAnchor = HudAnchor.MIDDLE_CENTER,
                scaleX = 2f,
                scaleY = 0.5f,
                x = 10,
                y = -5,
            ),
            parentWidth = 800,
            parentHeight = 600,
            elementWidth = 100,
            elementHeight = 40,
        )

        assertEquals(HudBounds(310f, 285f, 510f, 305f), result.bounds)
        assertEquals(400f, result.screenAnchorX)
        assertEquals(300f, result.screenAnchorY)
        assertEquals(410f, result.elementAnchorX)
        assertEquals(295f, result.elementAnchorY)
        assertEquals(35 to -25, result.offsetForElementAnchor(435f, 275f))
        assertEquals(50f to 20f, result.toLocal(410f, 295f))
        assertTrue(result.bounds.contains(310f, 285f))
        assertFalse(result.bounds.contains(309f, 285f))
    }

    @Test
    fun `negative scale still returns normalized bounds`() {
        val result = HudLayoutEngine.resolve(
            HudLayoutSpec(scaleX = -2f, scaleY = -3f),
            parentWidth = 100,
            parentHeight = 100,
            elementWidth = 10,
            elementHeight = 5,
        )

        assertEquals(HudBounds(-20f, -15f, 0f, 0f), result.bounds)
    }

    @Test
    fun `zero scale has no inverse coordinate`() {
        val result = HudLayoutEngine.resolve(
            HudLayoutSpec(scaleX = 0f),
            parentWidth = 100,
            parentHeight = 100,
            elementWidth = 10,
            elementHeight = 10,
        )

        assertNull(result.toLocal(0f, 0f))
    }
}
