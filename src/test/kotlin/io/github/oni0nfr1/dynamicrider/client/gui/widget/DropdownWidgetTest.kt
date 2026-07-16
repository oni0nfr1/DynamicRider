package io.github.oni0nfr1.dynamicrider.client.gui.widget

import net.minecraft.network.chat.Component
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.lwjgl.glfw.GLFW

class DropdownWidgetTest {
    @Test
    fun `keyboard selection changes the value and closes the popup`() {
        var committed: String? = null
        val widget = widget(selected = "first") { committed = it }

        widget.onPress()
        assertTrue(widget.isExpanded)
        assertTrue(widget.keyPressed(GLFW.GLFW_KEY_DOWN, 0, 0))
        assertTrue(widget.keyPressed(GLFW.GLFW_KEY_ENTER, 0, 0))

        assertFalse(widget.isExpanded)
        assertEquals("second", widget.selectedValue)
        assertEquals("second", committed)
    }

    @Test
    fun `escape closes the popup without changing the selection`() {
        var committed: String? = null
        val widget = widget(selected = "second") { committed = it }

        widget.onPress()
        assertTrue(widget.keyPressed(GLFW.GLFW_KEY_ESCAPE, 0, 0))

        assertFalse(widget.isExpanded)
        assertEquals("second", widget.selectedValue)
        assertEquals(null, committed)
    }

    @Test
    fun `empty dropdown is disabled and visible row count must be positive`() {
        val empty = DropdownWidget<String>(0, 0, 100, 20, emptyList(), null, 1) {}
        assertFalse(empty.active)
        assertThrows(IllegalArgumentException::class.java) {
            DropdownWidget(0, 0, 100, 20, entries(), "first", 0) {}
        }
    }

    private fun widget(selected: String, onSelected: (String) -> Unit): DropdownWidget<String> =
        DropdownWidget(0, 0, 100, 20, entries(), selected, 2, onSelected)

    private fun entries(): List<DropdownEntry<String>> = listOf(
        DropdownEntry("first", Component.literal("First")),
        DropdownEntry("second", Component.literal("Second")),
        DropdownEntry("third", Component.literal("Third")),
    )
}
