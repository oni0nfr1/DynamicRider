package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

/** Enter 또는 실제 focus 이탈 시 변경된 입력값을 한 번 적용하는 편집 상자다. */
class HudCommitEditBox(
    font: Font,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    message: Component,
    initialValue: String,
    private val onCommit: (String) -> Boolean,
    private val onCancel: () -> Unit,
) : EditBox(font, x, y, width, height, message) {
    private var acceptedValue = initialValue
    private var committing = false

    init {
        value = initialValue
    }

    /** 변경된 문자열을 적용하고 파싱 또는 검증 성공 여부를 반환한다. */
    fun commitPending(): Boolean {
        if (committing || value == acceptedValue) return true
        committing = true
        return try {
            onCommit(value).also { accepted ->
                if (accepted) acceptedValue = value
            }
        } finally {
            committing = false
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return when (keyCode) {
            GLFW.GLFW_KEY_ENTER,
            GLFW.GLFW_KEY_KP_ENTER,
            -> {
                if (commitPending()) isFocused = false
                true
            }
            GLFW.GLFW_KEY_ESCAPE -> {
                value = acceptedValue
                onCancel()
                isFocused = false
                true
            }
            else -> super.keyPressed(keyCode, scanCode, modifiers)
        }
    }

    override fun setFocused(focused: Boolean) {
        val lostFocus = isFocused && !focused
        if (lostFocus && !committing) commitPending()
        super.setFocused(focused)
    }
}
