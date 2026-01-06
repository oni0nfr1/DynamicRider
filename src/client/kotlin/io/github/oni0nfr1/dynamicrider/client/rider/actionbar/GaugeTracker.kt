package io.github.oni0nfr1.dynamicrider.client.rider.actionbar

import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.rider.RiderBackend
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import java.util.Optional

class GaugeTracker(
    override val stateManager: HudStateManager
): RiderBackend {
    companion object {
        const val GAUGE_TEXT = '|'
        private val GOLD: TextColor = TextColor.fromLegacyFormat(ChatFormatting.GOLD)
            ?: TextColor.fromRgb(0xFFAA00)
    }

    val gauge: MutableState<Double> = mutableStateOf(stateManager, 0.0)

    fun updateGauge(component: Component) {
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