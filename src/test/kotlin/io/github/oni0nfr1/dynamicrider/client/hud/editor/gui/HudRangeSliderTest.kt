package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudNumberType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

class HudRangeSliderTest {
    @Test
    fun `integer slider values omit a decimal part`() {
        assertEquals("12", formatHudSliderValue(12.9, HudNumberType.INT))
    }

    @Test
    fun `floating slider values show at most one decimal place`() {
        assertEquals("12", formatHudSliderValue(12.0, HudNumberType.FLOAT))
        assertEquals("12.3", formatHudSliderValue(12.25, HudNumberType.DOUBLE))
        assertEquals("123.4", formatHudSliderValue(123.4, HudNumberType.FLOAT))
    }

    @Test
    fun `floating slider values use a locale independent decimal separator`() {
        val previous = Locale.getDefault(Locale.Category.FORMAT)
        try {
            Locale.setDefault(Locale.Category.FORMAT, Locale.FRANCE)
            assertEquals("12.3", formatHudSliderValue(12.25, HudNumberType.FLOAT))
        } finally {
            Locale.setDefault(Locale.Category.FORMAT, previous)
        }
    }
}
