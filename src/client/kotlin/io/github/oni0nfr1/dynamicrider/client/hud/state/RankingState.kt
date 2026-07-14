package io.github.oni0nfr1.dynamicrider.client.hud.state

import net.minecraft.network.chat.Component
import java.util.UUID

sealed interface RankingState {
    data class Unavailable(
        val racers: Map<UUID, Racer> = emptyMap(),
        val localRacerId: UUID? = null,
    ) : RankingState

    data class Available(
        val entries: List<Entry>,
        val racers: Map<UUID, Racer>,
        val alive: Set<UUID>,
        val localRacerId: UUID?,
    ) : RankingState

    data class Racer(
        val uuid: UUID,
        val name: String,
    )

    data class Entry(
        val rank: Int,
        val racer: Racer,
        val sidebarValue: Int,
        val displayName: Component,
    )
}
