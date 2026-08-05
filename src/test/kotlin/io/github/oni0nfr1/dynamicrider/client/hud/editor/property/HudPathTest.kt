package io.github.oni0nfr1.dynamicrider.client.hud.editor.property

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HudPathTest {
    @Test
    fun `path supports parent child and append operations`() {
        val root = HudPath.of("element-1")
        val child = root.child("speedometer")
        val property = child.append(HudPath.of("layout", "x"))

        assertTrue(root.isSingle)
        assertFalse(child.isSingle)
        assertNull(root.parent)
        assertEquals(root, child.parent)
        assertEquals(listOf("element-1", "speedometer", "layout", "x"), property.segments)
    }

    @Test
    fun `string form escapes path separators and escape markers`() {
        val path = HudPath.of("custom.element", "style~variant", "color")
        val encoded = path.toString()

        assertEquals("custom~1element.style~0variant.color", encoded)
        assertEquals(path, HudPath.parse(encoded))
    }
}
