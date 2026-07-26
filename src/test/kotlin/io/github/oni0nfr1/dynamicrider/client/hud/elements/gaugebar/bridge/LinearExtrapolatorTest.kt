package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge

import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LinearExtrapolatorTest {
    @Test
    fun `does not extrapolate below minimum charge speed`() {
        val state = TestNitroKartState(
            speed = 49.99,
            accurateDriftState = true,
            nitroGauge = 0.25f,
        )
        val extrapolator = LinearExtrapolator(state)

        extrapolator.updateGauge(20f)

        assertEquals(0.25f, extrapolator.nitroGauge)
    }

    @Test
    fun `does not extrapolate while not drifting`() {
        val state = TestNitroKartState(
            speed = 200.0,
            accurateDriftState = false,
            nitroGauge = 0.25f,
        )
        val extrapolator = LinearExtrapolator(state)

        extrapolator.updateGauge(20f)

        assertEquals(0.25f, extrapolator.nitroGauge)
    }

    @Test
    fun `extrapolates at minimum charge speed while drifting`() {
        val state = TestNitroKartState(
            speed = 50.0,
            accurateDriftState = true,
            nitroGauge = 0.25f,
        )
        val extrapolator = LinearExtrapolator(state)

        extrapolator.updateGauge(20f)

        assertTrue(extrapolator.nitroGauge > 0.25f)
    }

    @Test
    fun `snaps to raw gauge when charge condition ends`() {
        val state = TestNitroKartState(
            speed = 100.0,
            accurateDriftState = true,
            nitroGauge = 0.25f,
        )
        val extrapolator = LinearExtrapolator(state)
        extrapolator.updateGauge(20f)
        assertTrue(extrapolator.nitroGauge > 0.25f)

        state.speed = 49.99
        extrapolator.updateGauge(1f)

        assertEquals(0.25f, extrapolator.nitroGauge)
    }

    private data class TestNitroKartState(
        override var speed: Double = 0.0,
        override var accurateDriftState: Boolean = false,
        override var nitroGauge: Float = 0f,
        override var isDrifting: Boolean = accurateDriftState,
        override var isBoosting: Boolean = false,
        override var maxBoost: Int = 0,
        override var nitro: Int = 0,
        override var teamNitro: Int = 0,
        override var teamBoostGaugeAvailable: Boolean = false,
        override var teamBoostGauge: Float = 0f,
    ) : NitroKartState
}
