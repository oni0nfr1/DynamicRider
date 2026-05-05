package io.github.oni0nfr1.dynamicrider.client.hud.elements.speedmeter

import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HexColorSerdes
import io.github.oni0nfr1.skid.client.api.engine.SpeedEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("jiu_tachometer")
data class JiuTachometerSpec(
    override val layout: HudLayoutSpec,
    val tachometerBackgroundScale: Float = 1.25f,
    val animationLengthSec: Float = 0.5f,
    val glowThreshold: Int = 100,
    @Serializable(with = HexColorSerdes::class)
    val normalDigitColor: Int = 0xFFE8E08A.toInt(),
    @Serializable(with = HexColorSerdes::class)
    val glowDigitColor: Int = 0xFF00FFFF.toInt(),
    @Serializable(with = HexColorSerdes::class)
    val offDigitColor: Int = 0x40000000,
    val unitText: String = "km/h",
    @Serializable(with = HexColorSerdes::class)
    val slotOverlayColor: Int = 0x40000000,
) : HudElementSpec<JiuTachometer, SpeedEngine>() {
    override fun requiredEngineClass(): Class<out SpeedEngine> = SpeedEngine::class.java

    override fun create(kart: KartRef.Specific<SpeedEngine>) = JiuTachometer(this, kart)
}
