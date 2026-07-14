package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge

import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState

class LinearInterpolator(
    private val state: NitroKartState,
    private val interpolationSpeed: Float = 1f,
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
        val maxStep = interpolationSpeed * deltaTicks.coerceAtLeast(0f)

        displayNitroGauge = interpolate(displayNitroGauge, nitroGaugeRaw, maxStep)
        displayTeamBoostGauge = interpolate(displayTeamBoostGauge, teamBoostGaugeRaw, maxStep)
    }

    private fun interpolate(current: Float, target: Float, maxStep: Float): Float {
        val delta = target - current
        return (current + delta.coerceIn(-maxStep, maxStep)).coerceIn(0f, 1f)
    }
}
