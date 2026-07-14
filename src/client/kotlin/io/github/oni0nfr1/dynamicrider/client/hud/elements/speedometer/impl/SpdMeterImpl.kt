package io.github.oni0nfr1.dynamicrider.client.hud.elements.speedometer.impl

import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedometer.Speedometer
import io.github.oni0nfr1.dynamicrider.client.hud.state.SpeedKartState

class SpdMeterImpl(private val state: SpeedKartState) : Speedometer {
    override val speed: Double
        get() = state.speed
}
