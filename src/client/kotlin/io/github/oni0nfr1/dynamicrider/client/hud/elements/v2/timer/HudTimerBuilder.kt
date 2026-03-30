package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.timer

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec

class HudTimerBuilder : HudElementBuilder<HudTimerSpec>() {
    var minWidth: Int = 100
    var txtColor: Int = 0xFFFFFFFF.toInt()

    override fun build(layout: HudLayoutSpec): HudTimerSpec {
        return HudTimerSpec(
            layout = layout,
            minWidth = minWidth.coerceAtLeast(0),
            txtColor = txtColor,
        )
    }
}
