package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge

import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import kotlin.math.exp

class ExponentialInterpolator(
    private val state: NitroKartState,
    private val smoothing: Double,
): GaugeBar {
    val nitroGaugeRaw: Float
        get() = state.nitroGauge
    val teamBoostGaugeRaw: Float
        get() = state.teamBoostGauge

    private var displayNitroGauge: Float = nitroGaugeRaw
    private var displayTeamBoostGauge: Float = teamBoostGaugeRaw

    override val nitroGauge: Float
        get() = displayNitroGauge.coerceIn(0f, 1f)

    override val teamBoostGauge: Float
        get() = displayTeamBoostGauge.coerceIn(0f, 1f)

    override fun updateGauge(deltaTicks: Float) {
        val follow = 1.0 - exp(-smoothing * deltaTicks.toDouble())

        displayNitroGauge += ((nitroGaugeRaw - displayNitroGauge) * follow).toFloat()
        displayTeamBoostGauge += ((teamBoostGaugeRaw - displayTeamBoostGauge) * follow).toFloat()
    }
}
