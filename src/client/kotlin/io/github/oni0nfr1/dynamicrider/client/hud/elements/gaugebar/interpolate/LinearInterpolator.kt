package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.interpolate

import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.rider.backend.bossbar.KartTeamBoostTracker
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

class LinearInterpolator(
    private val kart: KartRef.Specific<NitroEngine>,
    private val interpolationSpeed: Float = 1f,
): GaugeBar {
    val nitroGaugeRaw: Float
        get() = kart.accessEngine { it.tachometer?.gauge?.toFloat() } ?: 0.0f

    val teamBoostGaugeRaw: Float
        get() = if (KartTeamBoostTracker.gaugeExists) KartTeamBoostTracker.gauge else 0f

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
