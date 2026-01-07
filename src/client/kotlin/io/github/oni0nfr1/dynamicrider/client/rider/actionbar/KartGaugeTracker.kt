package io.github.oni0nfr1.dynamicrider.client.rider.actionbar

import io.github.oni0nfr1.dynamicrider.client.event.RiderTachometerCallback
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.state.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.state.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.rider.RiderBackend
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.world.InteractionResult
import java.util.Optional

class KartGaugeTracker(
    override val stateManager: HudStateManager
): RiderBackend, AutoCloseable {
    companion object {
        const val GAUGE_TEXT = '|'
        private val GOLD: TextColor = TextColor.fromLegacyFormat(ChatFormatting.GOLD)
            ?: TextColor.fromRgb(0xFFAA00)
    }

    private val eventListener: AutoCloseable = RiderTachometerCallback.EVENT.register { _, text, _ ->
        updateGauge(text)
        InteractionResult.PASS
    }

    val gauge: MutableState<Double> = mutableStateOf(stateManager, 0.0)

    private fun updateGauge(component: Component) {
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

    override fun close() {
        eventListener.close()
    }

}