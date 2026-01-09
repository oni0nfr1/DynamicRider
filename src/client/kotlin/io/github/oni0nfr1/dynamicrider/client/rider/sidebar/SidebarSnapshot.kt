package io.github.oni0nfr1.dynamicrider.client.rider.sidebar

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.Objective
import net.minecraft.world.scores.PlayerScoreEntry
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard

data class SidebarSnapshot(
    val title: Component,
    val objective: Objective?,
    val lines: List<SidebarLine>,
) {
    companion object {
        @JvmStatic
        fun fromMcClient(client: Minecraft = Minecraft.getInstance()): SidebarSnapshot? {
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

        fun emptySidebar() = SidebarSnapshot(Component.empty(), null, emptyList())
    }
}