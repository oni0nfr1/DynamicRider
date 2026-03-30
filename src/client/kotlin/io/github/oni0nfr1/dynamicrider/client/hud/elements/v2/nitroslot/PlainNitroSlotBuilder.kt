package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.nitroslot

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec

class PlainNitroSlotBuilder : HudElementBuilder<PlainNitroSlotSpec>() {
    var slotIndex: Int = 1
    var hideUntilOccupied: Boolean = false
    var keepVisibleAfterOccupied: Boolean = true
    var iconSize: Int = 32
    var boxPadding: Int = 5
    var boxColor: Int = 0x80000000.toInt()

    override fun build(layout: HudLayoutSpec): PlainNitroSlotSpec {
        return PlainNitroSlotSpec(
            layout = layout,
            slotIndex = slotIndex.coerceAtLeast(1),
            hideUntilOccupied = hideUntilOccupied,
            keepVisibleAfterOccupied = keepVisibleAfterOccupied,
            iconSize = iconSize.coerceAtLeast(0),
            boxPadding = boxPadding.coerceAtLeast(0),
            boxColor = boxColor,
        )
    }
}
