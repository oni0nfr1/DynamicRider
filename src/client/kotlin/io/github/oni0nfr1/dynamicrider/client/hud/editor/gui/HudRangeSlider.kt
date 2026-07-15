package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudNumberType
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudNumericRange
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import net.minecraft.client.gui.components.AbstractSliderButton
import net.minecraft.network.chat.Component
import kotlin.math.round

/** Drag 중에는 표시 값만 바꾸고 release 또는 keyboard 조작 완료 시 한 번만 값을 적용한다. */
class HudRangeSlider(
    x: Int,
    y: Int,
    width: Int,
    initialValue: Double,
    private val numberType: HudNumberType,
    private val range: HudNumericRange,
    private val onCommit: (JsonElement) -> Unit,
) : AbstractSliderButton(
    x,
    y,
    width,
    20,
    Component.empty(),
    normalize(initialValue, range),
) {
    init {
        updateMessage()
    }

    override fun updateMessage() {
        message = Component.literal(format(currentValue()))
    }

    override fun applyValue() {
        updateMessage()
    }

    override fun onRelease(mouseX: Double, mouseY: Double) {
        super.onRelease(mouseX, mouseY)
        commit()
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val handled = super.keyPressed(keyCode, scanCode, modifiers)
        if (handled) commit()
        return handled
    }

    private fun commit() {
        val number = currentValue()
        onCommit(
            when (numberType) {
                HudNumberType.BYTE -> JsonPrimitive(number.toInt().toByte())
                HudNumberType.SHORT -> JsonPrimitive(number.toInt().toShort())
                HudNumberType.INT -> JsonPrimitive(number.toInt())
                HudNumberType.LONG -> JsonPrimitive(number.toLong())
                HudNumberType.FLOAT -> JsonPrimitive(number.toFloat())
                HudNumberType.DOUBLE -> JsonPrimitive(number)
            }
        )
    }

    private fun currentValue(): Double {
        val raw = range.min + value * (range.max - range.min)
        val step = range.step ?: return raw.coerceIn(range.min, range.max)
        return (range.min + round((raw - range.min) / step) * step).coerceIn(range.min, range.max)
    }

    private fun format(value: Double): String = when (numberType) {
        HudNumberType.BYTE,
        HudNumberType.SHORT,
        HudNumberType.INT,
        HudNumberType.LONG,
        -> value.toLong().toString()
        HudNumberType.FLOAT,
        HudNumberType.DOUBLE,
        -> "%.3f".format(value).trimEnd('0').trimEnd('.')
    }

    private companion object {
        fun normalize(value: Double, range: HudNumericRange): Double {
            if (range.max == range.min) return 0.0
            return ((value - range.min) / (range.max - range.min)).coerceIn(0.0, 1.0)
        }
    }
}
