package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge

import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LinearInterpolatorTest {
    @Test
    fun `interpolates a new sample over one tick`() {
        val state = TestNitroKartState(nitroGauge = 0.2f)
        val interpolator = LinearInterpolator(state)

        state.nitroGauge = 0.8f
        interpolator.updateGauge(0.25f)
        assertEquals(0.35f, interpolator.nitroGauge, 0.00001f)

        interpolator.updateGauge(0.25f)
        assertEquals(0.5f, interpolator.nitroGauge, 0.00001f)

        interpolator.updateGauge(0.5f)
        assertEquals(0.8f, interpolator.nitroGauge, 0.00001f)
    }

    @Test
    fun `continues toward the target while the raw value is unchanged`() {
        val state = TestNitroKartState(nitroGauge = 0f)
        val interpolator = LinearInterpolator(state)

        state.nitroGauge = 1f
        interpolator.updateGauge(0.4f)
        interpolator.updateGauge(0.4f)

        assertEquals(0.8f, interpolator.nitroGauge, 0.00001f)
    }

    @Test
    fun `starts a replacement sample from the current display value`() {
        val state = TestNitroKartState(nitroGauge = 0f)
        val interpolator = LinearInterpolator(state)

        state.nitroGauge = 1f
        interpolator.updateGauge(0.25f)
        assertEquals(0.25f, interpolator.nitroGauge, 0.00001f)

        state.nitroGauge = 0.5f
        interpolator.updateGauge(0f)
        assertEquals(0.25f, interpolator.nitroGauge, 0.00001f)

        interpolator.updateGauge(0.5f)
        assertEquals(0.375f, interpolator.nitroGauge, 0.00001f)
    }

    @Test
    fun `interpolates nitro and team gauges independently`() {
        val state = TestNitroKartState(nitroGauge = 0f, teamBoostGauge = 1f)
        val interpolator = LinearInterpolator(state)

        state.nitroGauge = 1f
        state.teamBoostGauge = 0f
        interpolator.updateGauge(0.25f)

        assertEquals(0.25f, interpolator.nitroGauge, 0.00001f)
        assertEquals(0.75f, interpolator.teamBoostGauge, 0.00001f)
    }

    private data class TestNitroKartState(
        override var speed: Double = 0.0,
        override var accurateDriftState: Boolean = false,
        override var nitroGauge: Float = 0f,
        override var isDrifting: Boolean = false,
        override var isBoosting: Boolean = false,
        override var maxBoost: Int = 0,
        override var nitro: Int = 0,
        override var teamNitro: Int = 0,
        override var teamBoostGaugeAvailable: Boolean = false,
        override var teamBoostGauge: Float = 0f,
    ) : NitroKartState
}
