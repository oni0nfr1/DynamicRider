package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge

import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState

class RawGauge(private val state: NitroKartState): GaugeBar {
    override val nitroGauge: Float
        get() = state.nitroGauge

    override val teamBoostGauge: Float
        get() = state.teamBoostGauge

    override fun updateGauge(deltaTicks: Float) {}
}
