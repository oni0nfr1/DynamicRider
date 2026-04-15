package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.rankingtable

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec
import kotlinx.serialization.Serializable

@Serializable
data class PlainRankingTableSpec(
    override val layout: HudLayoutSpec,
    val defaultTextColor: Int = 0x00FFFFFF,
    val shadow: Boolean = true,
    val minWidth: Int = 100,
    val rowPadding: Int = 2,
    val paddingX: Int = 6,
    val paddingY: Int = 6,
    val backgroundColor: Int = 0x70000000,
    val headerBackgroundColor: Int = 0x90000000.toInt(),
    val highlightBackgroundColor: Int = 0x40FFFFC0,
    val dotSize: Int = 6,
    val dotGap: Int = 6,
    val hideWhenTimeAttack: Boolean = true,
) : HudElementSpec<PlainRankingTable>() {
    override fun create(): PlainRankingTable = PlainRankingTable(this)
}
