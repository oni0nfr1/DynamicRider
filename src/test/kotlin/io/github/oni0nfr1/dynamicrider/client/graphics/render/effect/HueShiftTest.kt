package io.github.oni0nfr1.dynamicrider.client.graphics.render.effect

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HueShiftTest {
    @Test
    fun `degrees are normalized to one positive rotation`() {
        assertEquals(0f, HueShift.normalizeDegrees(0f))
        assertEquals(0f, HueShift.normalizeDegrees(360f))
        assertEquals(300f, HueShift.normalizeDegrees(-60f))
        assertEquals(60f, HueShift.normalizeDegrees(420f))
        assertEquals(0f, HueShift.normalizeDegrees(Float.NaN))
    }

    @Test
    fun `vertex color preserves identity and clamps opacity`() {
        assertEquals(0xFFFFFFFF.toInt(), HueShift.encodeVertexColor(0f))
        assertEquals(0xFFFF_FFFF.toInt(), HueShift.encodeVertexColor(360f, 300))
        assertEquals(0x0055_FFFF, HueShift.encodeVertexColor(120f, -1))
    }
}
