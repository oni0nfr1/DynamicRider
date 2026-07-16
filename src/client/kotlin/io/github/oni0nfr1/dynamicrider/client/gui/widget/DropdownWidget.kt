package io.github.oni0nfr1.dynamicrider.client.gui.widget

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractButton
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/** [value]와 GUI에 표시할 [label]을 묶은 드롭다운 항목이다. */
data class DropdownEntry<T>(
    val value: T,
    val label: Component,
)

/** 선택 항목 하나를 표시하고 필요할 때 스크롤 가능한 목록을 펼치는 범용 위젯이다. */
class DropdownWidget<T>(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    entries: List<DropdownEntry<T>>,
    selected: T?,
    private val maxVisibleRows: Int,
    private val onSelected: (T) -> Unit,
) : AbstractButton(x, y, width, height, Component.empty()) {
    private val entries = entries.toList()
    private var selected = selected
    private var highlightedIndex = selectedIndex().takeIf { it >= 0 } ?: 0
    private var scrollOffset = 0

    var isExpanded: Boolean = false
        private set

    val selectedValue: T?
        get() = selected

    init {
        require(maxVisibleRows > 0) { "Dropdown must show at least one row" }
        active = entries.isNotEmpty()
        updateMessage()
        revealHighlighted()
    }

    override fun onPress() {
        if (entries.isEmpty()) return
        isExpanded = !isExpanded
        if (isExpanded) {
            highlightedIndex = selectedIndex().takeIf { it >= 0 } ?: 0
            revealHighlighted()
        }
        updateMessage()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isExpanded) return super.mouseClicked(mouseX, mouseY, button)
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false

        val entryIndex = entryIndexAt(mouseX, mouseY)
        if (entryIndex != null) {
            select(entryIndex)
            playDownSound(Minecraft.getInstance().soundManager)
        } else {
            close()
        }
        return true
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        scrollX: Double,
        scrollY: Double,
    ): Boolean {
        if (!isExpanded || !isOverPopup(mouseX, mouseY)) return false
        val direction = when {
            scrollY > 0.0 -> -1
            scrollY < 0.0 -> 1
            else -> 0
        }
        if (direction != 0) {
            scrollOffset = (scrollOffset + direction).coerceIn(0, maxScrollOffset())
        }
        return true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!isExpanded) return super.keyPressed(keyCode, scanCode, modifiers)
        return when (keyCode) {
            GLFW.GLFW_KEY_ESCAPE -> {
                close()
                true
            }
            GLFW.GLFW_KEY_UP -> {
                moveHighlight(-1)
                true
            }
            GLFW.GLFW_KEY_DOWN -> {
                moveHighlight(1)
                true
            }
            GLFW.GLFW_KEY_ENTER,
            GLFW.GLFW_KEY_KP_ENTER,
            GLFW.GLFW_KEY_SPACE,
            -> {
                if (entries.isNotEmpty()) select(highlightedIndex)
                true
            }
            else -> false
        }
    }

    /** 펼친 목록을 닫는다. */
    fun close() {
        if (!isExpanded) return
        isExpanded = false
        updateMessage()
    }

    /** 일반 위젯 렌더링이 끝난 뒤 펼친 목록을 최상단에 그린다. */
    fun renderPopup(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        if (!visible || !isExpanded) return
        val visibleEntries = visibleEntries()
        if (visibleEntries.isEmpty()) return

        val popupTop = y + height
        val popupBottom = popupTop + visibleEntries.size * height
        guiGraphics.pose().pushPose()
        guiGraphics.pose().translate(0f, 0f, POPUP_Z)
        guiGraphics.fill(x - 1, popupTop - 1, x + width + 1, popupBottom + 1, BORDER_COLOR)
        visibleEntries.forEachIndexed { row, indexedEntry ->
            val rowTop = popupTop + row * height
            val hovered = mouseX >= x && mouseX < x + width && mouseY >= rowTop && mouseY < rowTop + height
            val selected = indexedEntry.index == selectedIndex()
            val background = when {
                hovered -> HOVERED_COLOR
                selected -> SELECTED_COLOR
                else -> BACKGROUND_COLOR
            }
            guiGraphics.fill(x, rowTop, x + width, rowTop + height, background)
            guiGraphics.enableScissor(x + 2, rowTop, x + width - 2, rowTop + height)
            guiGraphics.drawCenteredString(
                Minecraft.getInstance().font,
                indexedEntry.value.label,
                x + width / 2,
                rowTop + (height - Minecraft.getInstance().font.lineHeight) / 2,
                TEXT_COLOR,
            )
            guiGraphics.disableScissor()
        }
        guiGraphics.pose().popPose()
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput)
    }

    private fun select(index: Int) {
        val entry = entries.getOrNull(index) ?: return
        selected = entry.value
        highlightedIndex = index
        isExpanded = false
        updateMessage()
        onSelected(entry.value)
    }

    private fun moveHighlight(offset: Int) {
        if (entries.isEmpty()) return
        highlightedIndex = (highlightedIndex + offset).coerceIn(0, entries.lastIndex)
        revealHighlighted()
    }

    private fun revealHighlighted() {
        if (highlightedIndex < scrollOffset) scrollOffset = highlightedIndex
        val lastVisibleIndex = scrollOffset + visibleRowCount() - 1
        if (highlightedIndex > lastVisibleIndex) scrollOffset = highlightedIndex - visibleRowCount() + 1
        scrollOffset = scrollOffset.coerceIn(0, maxScrollOffset())
    }

    private fun updateMessage() {
        val selectedLabel = entries.firstOrNull { it.value == selected }?.label ?: Component.empty()
        message = selectedLabel.copy().append(if (isExpanded) " ▲" else " ▼")
    }

    private fun selectedIndex(): Int = entries.indexOfFirst { it.value == selected }

    private fun visibleRowCount(): Int = minOf(entries.size, maxVisibleRows)

    private fun maxScrollOffset(): Int = (entries.size - visibleRowCount()).coerceAtLeast(0)

    private fun visibleEntries(): List<IndexedValue<DropdownEntry<T>>> = entries.withIndex()
        .drop(scrollOffset)
        .take(visibleRowCount())

    private fun entryIndexAt(mouseX: Double, mouseY: Double): Int? {
        if (!isOverPopup(mouseX, mouseY)) return null
        val row = ((mouseY - (y + height)) / height).toInt()
        return (scrollOffset + row).takeIf(entries.indices::contains)
    }

    private fun isOverPopup(mouseX: Double, mouseY: Double): Boolean {
        val popupTop = y + height
        val popupBottom = popupTop + visibleRowCount() * height
        return mouseX >= x && mouseX < x + width && mouseY >= popupTop && mouseY < popupBottom
    }

    private companion object {
        const val POPUP_Z = 1_000f
        const val BORDER_COLOR = 0xFFAAAAAA.toInt()
        const val BACKGROUND_COLOR = 0xFF202020.toInt()
        const val SELECTED_COLOR = 0xFF304B68.toInt()
        const val HOVERED_COLOR = 0xFF406A96.toInt()
        const val TEXT_COLOR = 0xFFFFFFFF.toInt()
    }
}
