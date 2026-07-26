package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge

import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState

class LinearInterpolator(
    private val state: NitroKartState,
): GaugeBar {
    private companion object {
        const val SAMPLE_INTERVAL_TICKS = 1f
    }

    private val nitroGaugeRaw: Float
        get() = state.nitroGauge

    private val teamBoostGaugeRaw: Float
        get() = state.teamBoostGauge

    private val nitroGaugeInterpolation = GaugeInterpolation(nitroGaugeRaw)
    private val teamBoostGaugeInterpolation = GaugeInterpolation(teamBoostGaugeRaw)

    override val nitroGauge: Float
        get() = nitroGaugeInterpolation.value

    override val teamBoostGauge: Float
        get() = teamBoostGaugeInterpolation.value

    override fun updateGauge(deltaTicks: Float) {
        nitroGaugeInterpolation.update(nitroGaugeRaw, deltaTicks)
        teamBoostGaugeInterpolation.update(teamBoostGaugeRaw, deltaTicks)
    }

    private class GaugeInterpolation(initialRawValue: Float) {
        private var observedRawValue: Float = initialRawValue.normalizedOr(0f)
        private var startValue: Float = observedRawValue
        private var targetValue: Float = observedRawValue
        private var elapsedTicks: Float = SAMPLE_INTERVAL_TICKS

        var value: Float = observedRawValue
            private set

        fun update(rawValue: Float, deltaTicks: Float) {
            val normalizedRawValue = rawValue.normalizedOr(observedRawValue)
            if (normalizedRawValue != observedRawValue) {
                observedRawValue = normalizedRawValue
                startValue = value
                targetValue = normalizedRawValue
                elapsedTicks = 0f
            }

            val elapsedDelta = deltaTicks
                .takeIf(Float::isFinite)
                ?.coerceAtLeast(0f)
                ?: 0f
            elapsedTicks = (elapsedTicks + elapsedDelta).coerceAtMost(SAMPLE_INTERVAL_TICKS)

            val progress = elapsedTicks / SAMPLE_INTERVAL_TICKS
            value = if (progress >= 1f) {
                targetValue
            } else {
                startValue + (targetValue - startValue) * progress
            }
        }

        private fun Float.normalizedOr(fallback: Float): Float =
            takeIf(Float::isFinite)?.coerceIn(0f, 1f) ?: fallback
    }
}
