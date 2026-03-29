package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.gaugebar

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec
import kotlinx.serialization.Serializable

@Serializable
data class GaugeBarSpec(
    override val layout: HudLayoutSpec,
    val thickness: Int = 8,
    val width: Int = 120,
    val padding: Int = 2,
    val boxColor: Int = 0x80000000.toInt(),
    val gaugeAlpha: Int = 0xFF,
    val targetGaugeAlpha: Int = 0x80,
    val smoothing: Double = 1.0,
    val gradientStops: List<GaugeBarStopSpec> = defaultGradientGaugeBarStops(),
) : HudElementSpec<GaugeBar>() {
    override fun create(): GaugeBar = GaugeBar(this)
}

fun defaultGradientGaugeBarStops(): List<GaugeBarStopSpec> = listOf(
    GaugeBarStopSpec(offset = 0, color = 0xFFFFFFFF.toInt()),
    GaugeBarStopSpec(offset = 30, color = 0xFFFFE8A1.toInt()),
    GaugeBarStopSpec(offset = 60, color = 0xFFFFC040.toInt()),
    GaugeBarStopSpec(offset = 90, color = 0xFFFF5E18.toInt()),
    GaugeBarStopSpec(offset = 120, color = 0xFFFF0000.toInt()),
)
