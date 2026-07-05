package io.github.oni0nfr1.dynamicrider.client.hud.elements.speedometer.impl

import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedometer.Speedometer
import io.github.oni0nfr1.skid.client.api.engine.SpeedEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

class SpdMeterImpl(val kart: KartRef.Specific<SpeedEngine>) : Speedometer {
    override val speed: Double
        get() = kart.accessEngine { it.tachometer?.speed } ?: 0.0
}