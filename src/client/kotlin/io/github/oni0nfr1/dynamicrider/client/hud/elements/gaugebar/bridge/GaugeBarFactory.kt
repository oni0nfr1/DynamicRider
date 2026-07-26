package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge

import io.github.oni0nfr1.dynamicrider.client.config.DynRiderConfig
import io.github.oni0nfr1.dynamicrider.client.config.GaugeInterpolationMode
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState

object GaugeBarFactory {
    private const val EXPONENTIAL_SMOOTHING = 1.0

    /**
     * Creates a gauge bridge using the configuration that is active now.
     *
     * The selected mode is captured by the returned instance, so changing the
     * configuration affects HUD elements created afterward without changing
     * elements in an existing scene.
     */
    fun createConfigured(state: NitroKartState): GaugeBar =
        create(state, DynRiderConfig.gaugeInterpolationMode)

    fun create(
        state: NitroKartState,
        mode: GaugeInterpolationMode,
    ): GaugeBar = when (mode) {
        GaugeInterpolationMode.RAW -> RawGauge(state)
        GaugeInterpolationMode.LINEAR_INTERPOLATION -> LinearInterpolator(state)
        GaugeInterpolationMode.EXPONENTIAL_INTERPOLATION ->
            ExponentialInterpolator(state, EXPONENTIAL_SMOOTHING)
        GaugeInterpolationMode.LINEAR_EXTRAPOLATION -> LinearExtrapolator(state)
    }
}
