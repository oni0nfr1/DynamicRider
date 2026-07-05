package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge

import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

class RawGauge(private val kart: KartRef.Specific<NitroEngine>): GaugeBar {
    override val nitroGauge: Float
        get() = kart.accessEngine { it.tachometer?.gauge?.toFloat() } ?: 0.0f

    override val teamBoostGauge: Float
        get() = kart.accessEngine { it.tachometer?.gauge?.toFloat() } ?: 0.0f

    override fun updateGauge(deltaTicks: Float) {}
}