package io.github.oni0nfr1.dynamicrider.client.hud.elements.rankingtable

import io.github.oni0nfr1.dynamicrider.client.hud.elements.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudLayoutSpec

class PlainRankingTableBuilder : HudElementBuilder<PlainRankingTableSpec>() {
    var defaultTextColor: Int = 0x00FFFFFF
    var shadow: Boolean = true
    var minWidth: Int = 100
    var rowPadding: Int = 2
    var paddingX: Int = 6
    var paddingY: Int = 6
    var backgroundColor: Int = 0x70000000
    var headerBackgroundColor: Int = 0x90000000.toInt()
    var highlightBackgroundColor: Int = 0x40FFFFC0
    var dotSize: Int = 6
    var dotGap: Int = 6
    var hideWhenTimeAttack: Boolean = true

    override fun build(layout: HudLayoutSpec): PlainRankingTableSpec {
        return PlainRankingTableSpec(
            layout = layout,
            defaultTextColor = defaultTextColor,
            shadow = shadow,
            minWidth = minWidth.coerceAtLeast(0),
            rowPadding = rowPadding.coerceAtLeast(0),
            paddingX = paddingX.coerceAtLeast(0),
            paddingY = paddingY.coerceAtLeast(0),
            backgroundColor = backgroundColor,
            headerBackgroundColor = headerBackgroundColor,
            highlightBackgroundColor = highlightBackgroundColor,
            dotSize = dotSize.coerceAtLeast(0),
            dotGap = dotGap.coerceAtLeast(0),
            hideWhenTimeAttack = hideWhenTimeAttack,
        )
    }
}
