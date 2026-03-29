package io.github.oni0nfr1.dynamicrider.client.rider.v2.actionbar

import io.github.oni0nfr1.dynamicrider.client.event.RiderActionBarCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import java.util.Optional

object KartGaugeTracker {
    const val GAUGE_TEXT = '|'
    private val GOLD: TextColor = TextColor.fromLegacyFormat(ChatFormatting.GOLD)
        ?: TextColor.fromRgb(0xFFAA00)

    private val eventListener: AutoCloseable = RiderActionBarCallback.EVENT.register { text, _ ->
        updateGauge(text)
        HandleResult.PASS
    }

    var gauge: Double = 0.0

    private fun updateGauge(component: Component) {
        var filledGauge = 0
        var totalGauge = 0

        component.visit({ style, text ->
            val gaugeCount = text.count { it == GAUGE_TEXT }
            totalGauge += gaugeCount
            if (style.color == GOLD) filledGauge += gaugeCount
            Optional.empty<Unit>()
        }, Style.EMPTY)

        if (totalGauge > 0) gauge = filledGauge.toDouble() / totalGauge.toDouble()
    }
}