package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar

interface GaugeBar {
    val nitroGauge: Float
    val teamBoostGauge: Float

    fun updateGauge(deltaTicks: Float) {}
}
