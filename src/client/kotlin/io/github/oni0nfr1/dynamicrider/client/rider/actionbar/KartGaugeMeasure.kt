package io.github.oni0nfr1.dynamicrider.client.rider.actionbar

import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.mutableStateOf
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import java.util.Optional

object KartGaugeMeasure {

    const val GAUGE_TEXT = '|'
    val GOLD: TextColor = TextColor.fromLegacyFormat(ChatFormatting.GOLD)
        ?: TextColor.fromRgb(0xFFAA00)

    var enabled = false
        set(value) {
            if (value == false) gauge.set(0.0)
            field = value
        }

    lateinit var stateManager: HudStateManager
    lateinit var gauge: MutableState<Double>

    fun init(stateManager: HudStateManager) {
        this.stateManager = stateManager
        gauge = mutableStateOf(stateManager, 0.0)
    }

    @JvmStatic
    fun updateGauge(component: Component) {
        if (!enabled) return

        var gaugeGold = 0

        component.visit(
            FormattedText.StyledContentConsumer<Unit> {
                style, text ->

                if (style.color == GOLD) {
                    gaugeGold += text.count { it == GAUGE_TEXT }
                }
                Optional.empty()
            }
        , Style.EMPTY)

        val gaugeValue = gaugeGold.toDouble() / 54.0
        gauge.set(gaugeValue)
    }

}