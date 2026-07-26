package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge

import io.github.oni0nfr1.dynamicrider.client.config.GaugeInterpolationMode
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class GaugeBarFactoryTest {
    private val state = TestNitroKartState()

    @Test
    fun `creates the implementation selected by the interpolation mode`() {
        assertInstanceOf(
            RawGauge::class.java,
            GaugeBarFactory.create(state, GaugeInterpolationMode.RAW),
        )
        assertInstanceOf(
            LinearInterpolator::class.java,
            GaugeBarFactory.create(state, GaugeInterpolationMode.LINEAR_INTERPOLATION),
        )
        assertInstanceOf(
            ExponentialInterpolator::class.java,
            GaugeBarFactory.create(state, GaugeInterpolationMode.EXPONENTIAL_INTERPOLATION),
        )
        assertInstanceOf(
            LinearExtrapolator::class.java,
            GaugeBarFactory.create(state, GaugeInterpolationMode.LINEAR_EXTRAPOLATION),
        )
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
