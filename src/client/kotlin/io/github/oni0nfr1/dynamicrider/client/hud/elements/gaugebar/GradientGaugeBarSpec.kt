package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar

import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HexColorSerdes
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("gradient_gauge_bar")
data class GradientGaugeBarSpec(
    override val layout: HudLayoutSpec,
    val thickness: Int = 8,
    val width: Int = 120,
    val padding: Int = 2,
    @Serializable(with = HexColorSerdes::class)
    val boxColor: Int = 0x80000000.toInt(),
    val gaugeAlpha: Int = 0xFF,
    val targetGaugeAlpha: Int = 0x80,
    val smoothing: Double = 1.0,
    val gradientStops: List<GradientGaugeBarStopSpec> = defaultGradientGaugeBarStops(),
) : HudElementSpec<GradientGaugeBar, NitroEngine>() {
    override fun requiredEngineClass(): Class<out NitroEngine> = NitroEngine::class.java

    override fun create(kart: KartRef.Specific<NitroEngine>) = GradientGaugeBar(this, kart)
}

fun defaultGradientGaugeBarStops(): List<GradientGaugeBarStopSpec> = listOf(
    GradientGaugeBarStopSpec(offset = 0, color = 0xFFFFFFFF.toInt()),
    GradientGaugeBarStopSpec(offset = 30, color = 0xFFFFE8A1.toInt()),
    GradientGaugeBarStopSpec(offset = 60, color = 0xFFFFC040.toInt()),
    GradientGaugeBarStopSpec(offset = 90, color = 0xFFFF5E18.toInt()),
    GradientGaugeBarStopSpec(offset = 120, color = 0xFFFF0000.toInt()),
)
