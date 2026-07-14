package io.github.oni0nfr1.dynamicrider.client.hud.runtime.state

import io.github.oni0nfr1.dynamicrider.client.hud.state.RankingState
import io.github.oni0nfr1.dynamicrider.client.rider.backend.sidebar.KartRankingManager
import net.minecraft.client.Minecraft

class LiveRankingState {
    fun current(): RankingState {
        val racers = KartRankingManager.racers.mapValues { (_, racer) ->
            RankingState.Racer(racer.uuid, racer.name)
        }
        val localRacerId = Minecraft.getInstance().player?.uuid
        if (KartRankingManager.isTimeAttack) {
            return RankingState.Unavailable(racers, localRacerId)
        }

        val entries = KartRankingManager.ranking.map { entry ->
            RankingState.Entry(
                rank = entry.rank,
                racer = racers.getValue(entry.racer.uuid),
                sidebarValue = entry.sidebarValue,
                displayName = entry.displayName,
            )
        }
        return RankingState.Available(
            entries = entries,
            racers = racers,
            alive = KartRankingManager.alive,
            localRacerId = localRacerId,
        )
    }
}
