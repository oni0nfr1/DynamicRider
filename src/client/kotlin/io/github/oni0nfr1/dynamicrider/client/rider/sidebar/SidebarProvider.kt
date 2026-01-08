package io.github.oni0nfr1.dynamicrider.client.rider.sidebar

import io.github.oni0nfr1.dynamicrider.client.util.schedule.Ticker
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.world.level.GameType
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.PlayerScoreEntry
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import java.util.UUID

object SidebarProvider {
    var enabled = false
        set(value) {
            if (value) {
                invalidate()
            } else {
                reset()
            }
            field = value
        }

    private var dirty = false
    private var scheduled: Ticker.TaskHandle? = null

    @JvmStatic
    fun invalidate() {
        if (!enabled) return

        dirty = true
        if (scheduled != null && !scheduled!!.isCancelled) return

        scheduled = Ticker.runTaskLater(delay = 0) {
            scheduled = null
            if (!dirty) return@runTaskLater
            dirty = false

            val snap = readSidebar() ?: return@runTaskLater

            RankingManager.updateRanking(snap)
        }
    }

    fun reset() {
        dirty = false
        scheduled?.cancel()
        scheduled = null
    }

    fun readSidebarTitle(client: Minecraft = Minecraft.getInstance()): Component? {
        val level = client.level ?: return null
        val scoreboard = level.scoreboard
        val obj = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR)
            ?: return null

        return obj.displayName
    }

    fun readSidebar(client: Minecraft = Minecraft.getInstance()): SidebarSnapshot? {
        val level = client.level ?: return null
        val scoreboard = level.scoreboard
        val obj = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR)
            ?: return emptySidebar()

        val title = obj.displayName
        val raw: Collection<PlayerScoreEntry> = scoreboard.listPlayerScores(obj)

        val sorted = raw
            .filterNot { it.owner().startsWith(Scoreboard.HIDDEN_SCORE_PREFIX) }
            .sortedWith(compareByDescending<PlayerScoreEntry> { it.value() }.thenBy { it.owner() })
            .take(15)

        val lines = sorted.mapIndexed { idx, e ->
            val owner = e.owner()
            val baseName = e.display() ?: Component.literal(owner)

            // 팀 prefix/suffix/색 적용(맵이 팀을 쓰면 이게 중요함)
            val team = scoreboard.getPlayersTeam(owner)
            val finalName =
                if (team != null) PlayerTeam.formatNameForTeam(team, baseName) else baseName

            SidebarLine(
                rank = idx + 1,
                owner = owner,
                name = finalName,
                value = e.value(),
            )
        }

        return SidebarSnapshot(title, obj, lines)
    }

    @JvmStatic
    fun onPlayerInfoRemove(profileIds: List<UUID>) {
        if (!RankingManager.enabled) return
        for (id in profileIds) {
            RankingManager.markEliminated(id, RankingManager.ElimReason.DISCONNECT)
        }
    }

    @JvmStatic
    fun onPlayerInfoUpdate(packet: ClientboundPlayerInfoUpdatePacket) {
        if (!RankingManager.enabled) return
        if (!packet.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE)) return

        for (e in packet.entries()) {
            if (e.gameMode() == GameType.SPECTATOR) {
                RankingManager.markEliminated(e.profileId(), RankingManager.ElimReason.SPECTATOR)
            }
        }
    }

    fun emptySidebar() = SidebarSnapshot(Component.empty(), null, emptyList())
}
