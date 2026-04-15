package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.gaugebar

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec
import kotlinx.serialization.Serializable

@Serializable
data class GradientGaugeBarSpec(
    override val layout: HudLayoutSpec,
    val thickness: Int = 8,
    val width: Int = 120,
    val padding: Int = 2,
    val boxColor: Int = 0x80000000.toInt(),
    val gaugeAlpha: Int = 0xFF,
    val targetGaugeAlpha: Int = 0x80,
    val smoothing: Double = 1.0,
    val gradientStops: List<GradientGaugeBarStopSpec> = defaultGradientGaugeBarStops(),
) : HudElementSpec<GradientGaugeBar>() {
    override fun create(): GradientGaugeBar = GradientGaugeBar(this)
}

fun defaultGradientGaugeBarStops(): List<GradientGaugeBarStopSpec> = listOf(
    GradientGaugeBarStopSpec(offset = 0, color = 0xFFFFFFFF.toInt()),
    GradientGaugeBarStopSpec(offset = 30, color = 0xFFFFE8A1.toInt()),
    GradientGaugeBarStopSpec(offset = 60, color = 0xFFFFC040.toInt()),
    GradientGaugeBarStopSpec(offset = 90, color = 0xFFFF5E18.toInt()),
    GradientGaugeBarStopSpec(offset = 120, color = 0xFFFF0000.toInt()),
)
