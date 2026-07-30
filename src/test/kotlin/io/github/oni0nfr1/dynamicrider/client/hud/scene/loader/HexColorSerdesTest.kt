package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class HexColorSerdesTest {
    @Test
    fun `parses supported RGB and ARGB input forms`() {
        assertEquals(0xFFAABBCC.toInt(), HexColorSerdes.parse("#AABBCC"))
        assertEquals(0x80AABBCC.toInt(), HexColorSerdes.parse("#80AABBCC"))
        assertEquals(0xFFAABBCC.toInt(), HexColorSerdes.parse("0xAABBCC"))
        assertEquals(0x80AABBCC.toInt(), HexColorSerdes.parse("80AABBCC"))
    }

    @Test
    fun `rejects invalid color length and digits`() {
        assertThrows(IllegalArgumentException::class.java) {
            HexColorSerdes.parse("#ABC")
        }
        assertThrows(NumberFormatException::class.java) {
            HexColorSerdes.parse("#GGGGGG")
        }
    }
}
