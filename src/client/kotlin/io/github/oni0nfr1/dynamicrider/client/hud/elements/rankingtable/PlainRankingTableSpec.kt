package io.github.oni0nfr1.dynamicrider.client.hud.elements.rankingtable

import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HexColorSerdes
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("plain_ranking_table")
data class PlainRankingTableSpec(
    override val layout: HudLayoutSpec,
    @Serializable(with = HexColorSerdes::class)
    val defaultTextColor: Int = 0x00FFFFFF,
    val shadow: Boolean = true,
    val minWidth: Int = 100,
    val rowPadding: Int = 2,
    val paddingX: Int = 6,
    val paddingY: Int = 6,
    @Serializable(with = HexColorSerdes::class)
    val backgroundColor: Int = 0x70000000,
    @Serializable(with = HexColorSerdes::class)
    val headerBackgroundColor: Int = 0x90000000.toInt(),
    @Serializable(with = HexColorSerdes::class)
    val highlightBackgroundColor: Int = 0x40FFFFC0,
    val dotSize: Int = 6,
    val dotGap: Int = 6,
    val hideWhenTimeAttack: Boolean = true,
) : HudElementSpec<PlainRankingTable, KartEngine>() {
    override fun requiredEngineClass(): Class<out KartEngine> = KartEngine::class.java

    override fun create(kart: KartRef.Specific<KartEngine>): PlainRankingTable = PlainRankingTable(this, kart)
}
