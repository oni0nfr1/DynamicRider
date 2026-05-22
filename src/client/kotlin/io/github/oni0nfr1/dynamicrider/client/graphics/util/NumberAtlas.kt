package io.github.oni0nfr1.dynamicrider.client.graphics.util

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class NumberAtlas(
    texture: ResourceLocation,
    private val digitWidth: Int,
    private val digitHeight: Int,
    private val digitOrder: String = "0123456789",
) : Atlas(
    texture = texture,
    width = digitWidth * digitOrder.length,
    height = digitHeight,
    cellWidth = digitWidth,
    cellHeight = digitHeight,
) {
    init {
        require(digitOrder.length == 10) {
            "digitOrder must contain exactly 10 digits"
        }
        require(digitOrder.toSet() == ('0'..'9').toSet()) {
            "digitOrder must contain each digit from 0 to 9 exactly once"
        }
    }

    enum class Anchor(
        private val xNumerator: Int,
        private val yNumerator: Int,
    ) {
        TOP_LEFT(0, 0),
        TOP_CENTER(1, 0),
        TOP_RIGHT(2, 0),
        MIDDLE_LEFT(0, 1),
        MIDDLE_CENTER(1, 1),
        MIDDLE_RIGHT(2, 1),
        BOTTOM_LEFT(0, 2),
        BOTTOM_CENTER(1, 2),
        BOTTOM_RIGHT(2, 2);

        fun offsetX(width: Int): Int {
            return width * xNumerator / 2
        }

        fun offsetY(height: Int): Int {
            return height * yNumerator / 2
        }
    }

    fun drawDigit(
        guiGraphics: GuiGraphics,
        digit: Int,
        x: Int,
        y: Int,
        color: Int = 0xFFFFFFFF.toInt(),
    ) {
        cellAt(0, digitCellIndex(digit)).draw(
            guiGraphics = guiGraphics,
            x = x,
            y = y,
            color = color,
        )
    }

    fun drawNumber(
        guiGraphics: GuiGraphics,
        number: Int,
        x: Int,
        y: Int,
        anchor: Anchor = Anchor.TOP_LEFT,
        minDigits: Int = 1,
        spacing: Int = 0,
        color: Int = 0xFFFFFFFF.toInt(),
    ) {
        require(number >= 0) {
            "number must be greater than or equal to 0: $number"
        }
        require(minDigits >= 1) {
            "minDigits must be greater than or equal to 1: $minDigits"
        }
        require(spacing >= 0) {
            "spacing must be greater than or equal to 0: $spacing"
        }

        val text = number.toString().padStart(minDigits, '0')
        val textWidth = text.length * digitWidth + (text.length - 1) * spacing
        val drawX = x - anchor.offsetX(textWidth)
        val drawY = y - anchor.offsetY(digitHeight)

        text.forEachIndexed { index, digit ->
            drawDigit(
                guiGraphics = guiGraphics,
                digit = digit.digitToInt(),
                x = drawX + index * (digitWidth + spacing),
                y = drawY,
                color = color,
            )
        }
    }

    private fun digitCellIndex(digit: Int): Int {
        require(digit in 0..9) {
            "digit must be in 0..9: $digit"
        }

        return digitOrder.indexOf(('0'.code + digit).toChar())
    }
}
