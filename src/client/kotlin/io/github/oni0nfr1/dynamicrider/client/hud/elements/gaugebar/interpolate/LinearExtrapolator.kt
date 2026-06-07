package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.interpolate

import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.rider.backend.bossbar.KartTeamBoostTracker
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

class LinearExtrapolator(
    private val kart: KartRef.Specific<NitroEngine>,
) : GaugeBar {
    val nitroGaugeRaw: Float
        get() = kart.accessEngine { it.tachometer?.gauge?.toFloat() } ?: 0.0f
    val teamBoostGaugeRaw: Float
        get() = if (KartTeamBoostTracker.gaugeExists) KartTeamBoostTracker.gauge else 0f

    private var lastNitroGaugeRaw: Float = nitroGaugeRaw
    private var lastTeamBoostGaugeRaw: Float = teamBoostGaugeRaw

    private var displayNitroGauge: Float = lastNitroGaugeRaw
    private var displayTeamBoostGauge: Float = lastTeamBoostGaugeRaw

    private var nitroChargeCoefficient: Float = 0f
    private var nitroChargeSampleCount: Int = 0

    private val speed: Float
        get() = kart.accessEngine { it.tachometer?.speed?.toFloat() } ?: 0f

    private val drifting: Boolean
        get() = kart.accessEngine { it.isDrifting } ?: false

    override val nitroGauge: Float
        get() = displayNitroGauge.coerceIn(0f, 1f)

    override val teamBoostGauge: Float
        get() = displayTeamBoostGauge.coerceIn(0f, 1f)

    override fun updateGauge(deltaTicks: Float) {
        val currentSpeed = speed.coerceAtLeast(0f)
        val extrapolate = drifting
        updateNitroGauge(currentSpeed, deltaTicks, extrapolate)
        updateTeamBoostGauge(deltaTicks)
    }

    private fun updateNitroGauge(currentSpeed: Float, deltaTicks: Float, extrapolate: Boolean) {
        val currentNitroGaugeRaw = nitroGaugeRaw
        val rawDelta = currentNitroGaugeRaw - lastNitroGaugeRaw

        if (rawDelta > 0f) {
            if (extrapolate && currentSpeed > 0f) {
                nitroChargeCoefficient = updateAverage(
                    average = nitroChargeCoefficient,
                    sampleCount = nitroChargeSampleCount,
                    sample = rawDelta / currentSpeed,
                )
                nitroChargeSampleCount += 1
            }
            displayNitroGauge = maxOf(displayNitroGauge, currentNitroGaugeRaw)
        } else if (rawDelta < 0f) {
            displayNitroGauge = currentNitroGaugeRaw
        } else if (extrapolate) {
            displayNitroGauge += nitroChargeCoefficient * currentSpeed * deltaTicks
        }

        displayNitroGauge = displayNitroGauge.coerceIn(0f, 1f)
        lastNitroGaugeRaw = currentNitroGaugeRaw
    }

    private fun updateTeamBoostGauge(deltaTicks: Float) {
        val currentTeamBoostGaugeRaw = teamBoostGaugeRaw
        val delta = currentTeamBoostGaugeRaw - displayTeamBoostGauge
        val maxStep = deltaTicks.coerceAtLeast(0f)

        displayTeamBoostGauge += delta.coerceIn(-maxStep, maxStep)

        if (currentTeamBoostGaugeRaw < lastTeamBoostGaugeRaw) {
            displayTeamBoostGauge = minOf(displayTeamBoostGauge, currentTeamBoostGaugeRaw)
        }

        displayTeamBoostGauge = displayTeamBoostGauge.coerceIn(0f, 1f)
        lastTeamBoostGaugeRaw = currentTeamBoostGaugeRaw
    }

    private fun updateAverage(
        average: Float,
        sampleCount: Int,
        sample: Float,
    ): Float {
        if (!sample.isFinite()) return average
        if (sampleCount <= 0) return sample

        return average + (sample - average) / (sampleCount + 1)
    }
}
