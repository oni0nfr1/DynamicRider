package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.speedmeter

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec
import kotlinx.serialization.Serializable

@Serializable
data class JiuTachometerSpec(
    override val layout: HudLayoutSpec,
    val tachometerBackgroundScale: Float = 1.25f,
    val animationLengthSec: Float = 0.5f,
    val glowThreshold: Int = 100,
    val normalDigitColor: Int = 0xFFE8E08A.toInt(),
    val glowDigitColor: Int = 0xFF00FFFF.toInt(),
    val offDigitColor: Int = 0x40000000,
    val unitText: String = "km/h",
    val slotOverlayColor: Int = 0x40000000,
) : HudElementSpec<JiuTachometer>() {
    override fun create(): JiuTachometer = JiuTachometer(this)
}
