package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge

import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState

class LinearExtrapolator(
    private val state: NitroKartState,
) : GaugeBar {
    companion object {
        private const val SPEED_SCORE_SCALE = 139.0
        private const val DYNAMIC_CHARGE_DIVISOR = 360.0
        private const val MAX_GAUGE_SCORE = 2000.0
        private const val SPEED_CHARGE_COEFFICIENT =
            SPEED_SCORE_SCALE / DYNAMIC_CHARGE_DIVISOR / MAX_GAUGE_SCORE
    }

    val nitroGaugeRaw: Float
        get() = state.nitroGauge
    val teamBoostGaugeRaw: Float
        get() = state.teamBoostGauge

    private var lastNitroGaugeRaw: Float = nitroGaugeRaw
    private var lastTeamBoostGaugeRaw: Float = teamBoostGaugeRaw

    private var displayNitroGauge: Float = lastNitroGaugeRaw
    private var displayTeamBoostGauge: Float = lastTeamBoostGaugeRaw

    // A gauge step represents all charge accumulated since the previous visible step.
    private var intervalSpeedTicks: Double = 0.0
    private var intervalTicks: Double = 0.0
    private var intervalStartsAtGaugeBoundary: Boolean = false

    // Least-squares sums for: gaugeDelta - fixedSpeedCharge = baseRate * ticks.
    private var ticksSquaredSum: Double = 0.0
    private var ticksTimesBaseChargeSum: Double = 0.0

    private var baseChargePerTick: Double = 0.0

    private val speed: Float
        get() = state.speed.toFloat()

    private val drifting: Boolean
        get() = state.isDrifting

    private var wasDrifting: Boolean = drifting

    override val nitroGauge: Float
        get() = displayNitroGauge.coerceIn(0f, 1f)

    override val teamBoostGauge: Float
        get() = displayTeamBoostGauge.coerceIn(0f, 1f)

    override fun updateGauge(deltaTicks: Float) {
        val currentSpeed = speed.coerceAtLeast(0f)
        val extrapolate = drifting
        updateNitroGauge(
            currentSpeed = currentSpeed,
            deltaTicks = deltaTicks,
            extrapolate = extrapolate,
            snapToRaw = wasDrifting && !extrapolate,
        )
        wasDrifting = extrapolate
        updateTeamBoostGauge(deltaTicks)
    }

    private fun updateNitroGauge(
        currentSpeed: Float,
        deltaTicks: Float,
        extrapolate: Boolean,
        snapToRaw: Boolean,
    ) {
        val currentNitroGaugeRaw = nitroGaugeRaw
        val rawDelta = currentNitroGaugeRaw - lastNitroGaugeRaw
        val elapsedTicks = deltaTicks
            .takeIf { it.isFinite() }
            ?.coerceAtLeast(0f)
            ?.toDouble()
            ?: 0.0

        if (extrapolate) {
            intervalSpeedTicks += currentSpeed.toDouble() * elapsedTicks
            intervalTicks += elapsedTicks
        } else if (!snapToRaw) {
            resetSamplingInterval()
        }

        if (snapToRaw) {
            if (rawDelta > 0f && intervalStartsAtGaugeBoundary && intervalTicks > 0.0) {
                sampleChargeSpec(rawDelta.toDouble())
            }
            displayNitroGauge = currentNitroGaugeRaw
            resetSamplingInterval()
        } else if (rawDelta > 0f) {
            if (extrapolate && intervalTicks > 0.0) {
                // The first interval starts at an unknown point inside a quantized gauge step.
                if (intervalStartsAtGaugeBoundary) {
                    sampleChargeSpec(rawDelta.toDouble())
                }
                intervalStartsAtGaugeBoundary = true
                clearSamplingInterval()
            }
            displayNitroGauge = maxOf(displayNitroGauge, currentNitroGaugeRaw)
        } else if (rawDelta < 0f) {
            displayNitroGauge = currentNitroGaugeRaw
            resetSamplingInterval()
        } else if (extrapolate) {
            displayNitroGauge += (chargeRate(currentSpeed) * elapsedTicks).toFloat()
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

    private fun sampleChargeSpec(gaugeDelta: Double) {
        if (!gaugeDelta.isFinite() || gaugeDelta <= 0.0) return

        val baseCharge = gaugeDelta - SPEED_CHARGE_COEFFICIENT * intervalSpeedTicks
        ticksSquaredSum += intervalTicks * intervalTicks
        ticksTimesBaseChargeSum += intervalTicks * baseCharge

        baseChargePerTick = (ticksTimesBaseChargeSum / ticksSquaredSum)
            .takeIf { it.isFinite() }
            ?.coerceAtLeast(0.0)
            ?: baseChargePerTick
    }

    private fun chargeRate(currentSpeed: Float): Double {
        return (SPEED_CHARGE_COEFFICIENT * currentSpeed + baseChargePerTick)
            .coerceAtLeast(0.0)
    }

    private fun resetSamplingInterval() {
        clearSamplingInterval()
        intervalStartsAtGaugeBoundary = false
    }

    private fun clearSamplingInterval() {
        intervalSpeedTicks = 0.0
        intervalTicks = 0.0
    }
}
