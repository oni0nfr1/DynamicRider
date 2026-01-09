package io.github.oni0nfr1.dynamicrider.client.hud.elements

import io.github.oni0nfr1.dynamicrider.client.hud.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.RankingTable
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.KartRankingManager
import io.github.oni0nfr1.dynamicrider.client.util.ordinal
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import java.util.UUID

class PlainRankingTable(
    manager: HudStateManager,
    composer: PlainRankingTable.() -> Unit
) : HudElementImpl<PlainRankingTable>(manager, composer), RankingTable {

    /** 텍스트 */
    var defaultTextColor = 0xFFFFFF
    var shadow = true
    var font: Font = Minecraft.getInstance().font

    /** 전체 박스 폭 */
    var width = 150

    /** 행 높이 */
    var rowPadding = 2
    val rowHeight
        get() = font.lineHeight + rowPadding * 2

    /** padding */
    var paddingX = 6
    var paddingY = 6

    /** 배경 */
    var backgroundColor = 0x70000000
    var headerBackgroundColor = 0x90000000.toInt()
    var highlightBackgroundColor = 0x40FFFFC0

    /** 아이콘(점) */
    var dotSize = 6
    var dotGap = 6

    var hide = false
    override var ranking: List<KartRankingManager.RankingEntry> = emptyList()
    override var racers: LinkedHashMap<UUID, KartRankingManager.Racer> = linkedMapOf()
    override var eliminated: LinkedHashMap<UUID, KartRankingManager.ElimReason> = linkedMapOf()
    override var alive: LinkedHashSet<UUID> = linkedSetOf()

    override fun resolveSize() {
        val rows = racers.size
        val headerH = rowHeight + 2
        val h = paddingY * 2 + headerH + rows * rowHeight
        setSize(width, h)
    }

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        if (hide) return
        val client = Minecraft.getInstance()
        val myUuid = client.player?.uuid

        guiGraphics.fill(
            0, 0,
            size.x, size.y,
            backgroundColor
        )

        var y = paddingY
        guiGraphics.fill(
            0, y,
            size.x, y + rowHeight,
            headerBackgroundColor
        )
        val myRank = ranking.firstOrNull { it.racer.uuid == myUuid }?.rank
        val headerText = "${myRank?.ordinal() ?: "--"} / ${racers.size}"
        guiGraphics.drawString(
            font,
            headerText,
            paddingX, y + rowPadding,
            defaultTextColor, shadow
        )
        y += rowHeight

        var rowY = y
        ranking.forEach { entry ->
            if (entry.racer.uuid !in alive) return@forEach

            if (entry.racer.uuid == myUuid) {
                guiGraphics.fill(
                    0, rowY,
                    size.x, rowY + rowHeight,
                    highlightBackgroundColor
                )
            }

            val rgb = entry.displayName.style.color?.value
                ?: defaultTextColor
            val dotColor = argb(0xFF, rgb)
            val dotX = paddingX
            val dotY = rowY + (rowHeight - dotSize) / 2
            guiGraphics.fill(
                dotX, dotY,
                dotX + dotSize, dotY + dotSize,
                dotColor
            )

            val nameX = dotX + dotSize + dotGap
            val nameY = rowY + (rowHeight - font.lineHeight) / 2
            guiGraphics.drawString(
                font,
                entry.displayName,
                nameX, nameY,
                defaultTextColor, shadow
            )

            rowY += rowHeight
        }
    }

    private fun argb(a: Int, rgb: Int): Int = (a shl 24) or (rgb and 0xFFFFFF)
}
