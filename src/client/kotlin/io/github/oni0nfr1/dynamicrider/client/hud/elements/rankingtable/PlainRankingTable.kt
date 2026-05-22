package io.github.oni0nfr1.dynamicrider.client.hud.elements.rankingtable

import io.github.oni0nfr1.dynamicrider.client.graphics.util.textWithDynriderFont
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HexColorSerdes
import io.github.oni0nfr1.dynamicrider.client.rider.backend.sidebar.KartRankingManager
import io.github.oni0nfr1.dynamicrider.client.util.ordinal
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import java.util.UUID
import kotlin.math.max

class PlainRankingTable(
    spec: Spec,
    kart: KartRef.Specific<KartEngine>,
) : HudElementImpl<KartEngine>(spec.layout, kart) {
    var defaultTextColor: Int = spec.defaultTextColor
    var shadow: Boolean = spec.shadow
    var minWidth: Int = spec.minWidth
    var rowPadding: Int = spec.rowPadding
    var paddingX: Int = spec.paddingX
    var paddingY: Int = spec.paddingY
    var backgroundColor: Int = spec.backgroundColor
    var headerBackgroundColor: Int = spec.headerBackgroundColor
    var highlightBackgroundColor: Int = spec.highlightBackgroundColor
    var dotSize: Int = spec.dotSize
    var dotGap: Int = spec.dotGap
    var hideWhenTimeAttack: Boolean = spec.hideWhenTimeAttack

    private val fontManager: Font
        get() = Minecraft.getInstance().font

    private val rowHeight: Int
        get() = fontManager.lineHeight + rowPadding * 2

    private var hidden: Boolean = false
    private var ranking: List<KartRankingManager.RankingEntry> = emptyList()
    private var racers: LinkedHashMap<UUID, KartRankingManager.Racer> = linkedMapOf()
    private var alive: LinkedHashSet<UUID> = linkedSetOf()

    override fun resolveSize() {
        syncState()
        if (hidden) {
            setSize(0, 0)
            return
        }

        val visibleEntries = ranking.filter { it.racer.uuid in alive }
        val headerText = headerText()
        val headerWidth = fontManager.width(headerText)
        val widestRowWidth = visibleEntries.maxOfOrNull { entry ->
            dotSize + dotGap + fontManager.width(entry.displayName.string)
        } ?: 0

        val contentWidth = max(headerWidth, widestRowWidth)
        val width = max(minWidth, contentWidth + paddingX * 2)
        val height = paddingY * 2 + rowHeight + visibleEntries.size * rowHeight
        setSize(width, height)
    }

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        syncState()
        if (hidden) return

        val visibleEntries = ranking.filter { it.racer.uuid in alive }
        val myUuid = Minecraft.getInstance().player?.uuid

        guiGraphics.fill(0, 0, size.x, size.y, backgroundColor)

        var cursorY = paddingY
        guiGraphics.fill(0, cursorY, size.x, cursorY + rowHeight, headerBackgroundColor)
        guiGraphics.textWithDynriderFont(
            paddingX,
            cursorY + rowPadding,
            defaultTextColor,
            headerText(),
            shadow,
        )
        cursorY += rowHeight

        visibleEntries.forEach { entry ->
            if (entry.racer.uuid == myUuid) {
                guiGraphics.fill(
                    0,
                    cursorY,
                    size.x,
                    cursorY + rowHeight,
                    highlightBackgroundColor,
                )
            }

            val rgb = entry.displayName.style.color?.value ?: defaultTextColor
            val dotColor = argb(0xFF, rgb)
            val dotX = paddingX
            val dotY = cursorY + (rowHeight - dotSize) / 2
            guiGraphics.fill(
                dotX,
                dotY,
                dotX + dotSize,
                dotY + dotSize,
                dotColor,
            )

            val nameX = dotX + dotSize + dotGap
            val nameY = cursorY + (rowHeight - fontManager.lineHeight) / 2
            guiGraphics.textWithDynriderFont(
                nameX,
                nameY,
                defaultTextColor,
                entry.displayName.string,
                shadow,
            )

            cursorY += rowHeight
        }
    }

    private fun syncState() {
        hidden = hideWhenTimeAttack && KartRankingManager.isTimeAttack
        ranking = KartRankingManager.ranking
        racers = KartRankingManager.racers
        alive = KartRankingManager.alive

        if (racers.isEmpty()) {
            hidden = true
        }
    }

    private fun headerText(): String {
        val myUuid = Minecraft.getInstance().player?.uuid
        val myRank = ranking.firstOrNull { it.racer.uuid == myUuid }?.rank
        return "${myRank?.ordinal() ?: "--"} / ${racers.size}"
    }

    private fun argb(a: Int, rgb: Int): Int = (a shl 24) or (rgb and 0x00FFFFFF)

    class Builder : HudElementBuilder<Spec>() {
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

        override fun build(layout: HudLayoutSpec): Spec {
            return Spec(
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

    @Serializable
    @SerialName("PLAIN_RANKING_TABLE")
    data class Spec(
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
}
