package io.github.oni0nfr1.dynamicrider.client.hud.elements.speedmeter

import io.github.oni0nfr1.dynamicrider.client.hud.elements.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudLayoutSpec

class JiuTachometerBuilder : HudElementBuilder<JiuTachometerSpec>() {
    var tachometerBackgroundScale: Float = 1.25f
    var animationLengthSec: Float = 0.5f
    var glowThreshold: Int = 100
    var normalDigitColor: Int = 0xFFE8E08A.toInt()
    var glowDigitColor: Int = 0xFF00FFFF.toInt()
    var offDigitColor: Int = 0x40000000
    var unitText: String = "km/h"
    var slotOverlayColor: Int = 0x40000000

    override fun build(layout: HudLayoutSpec): JiuTachometerSpec {
        return JiuTachometerSpec(
            layout = layout,
            tachometerBackgroundScale = tachometerBackgroundScale.coerceAtLeast(0f),
            animationLengthSec = animationLengthSec.coerceAtLeast(0.01f),
            glowThreshold = glowThreshold.coerceAtLeast(0),
            normalDigitColor = normalDigitColor,
            glowDigitColor = glowDigitColor,
            offDigitColor = offDigitColor,
            unitText = unitText,
            slotOverlayColor = slotOverlayColor,
        )
    }
}
