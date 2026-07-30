package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HexColorSerdes
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component

/** 색상 입력 문자열에서 마지막으로 확인된 유효한 색상을 표시한다. */
class HudColorSwatch(
    x: Int,
    y: Int,
    size: Int,
    initialValue: String,
    message: Component,
) : AbstractWidget(x, y, size, size, message) {
    private var previewColor: Int = runCatching { HexColorSerdes.parse(initialValue) }
        .getOrDefault(0x00000000)

    init {
        active = false
    }

    /** 유효한 색상 문자열일 때만 preview를 갱신한다. */
    fun preview(value: String) {
        runCatching { HexColorSerdes.parse(value) }
            .onSuccess { previewColor = it }
    }

    override fun renderWidget(
        guiGraphics: GuiGraphics,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        guiGraphics.fill(x, y, x + width, y + height, BORDER_COLOR)
        val left = x + 2
        val top = y + 2
        val right = x + width - 2
        val bottom = y + height - 2
        for (cellY in top until bottom step CHECKER_SIZE) {
            for (cellX in left until right step CHECKER_SIZE) {
                val checkerColor = if (
                    ((cellX - left) / CHECKER_SIZE + (cellY - top) / CHECKER_SIZE) % 2 == 0
                ) {
                    CHECKER_LIGHT
                } else {
                    CHECKER_DARK
                }
                guiGraphics.fill(
                    cellX,
                    cellY,
                    minOf(cellX + CHECKER_SIZE, right),
                    minOf(cellY + CHECKER_SIZE, bottom),
                    checkerColor,
                )
            }
        }
        guiGraphics.fill(left, top, right, bottom, previewColor)
    }

    override fun updateWidgetNarration(narrationElementOutput: NarrationElementOutput) = Unit

    private companion object {
        const val CHECKER_SIZE = 4
        const val BORDER_COLOR = 0xFF808080.toInt()
        const val CHECKER_LIGHT = 0xFFFFFFFF.toInt()
        const val CHECKER_DARK = 0xFF909090.toInt()
    }
}
