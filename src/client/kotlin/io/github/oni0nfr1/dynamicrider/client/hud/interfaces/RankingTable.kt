package io.github.oni0nfr1.dynamicrider.client.hud.interfaces

import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.KartRankingManager
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.KartRankingManager.ElimReason
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.KartRankingManager.Racer
import java.util.UUID

interface RankingTable: HudElement {

    var ranking: List<KartRankingManager.RankingEntry>
    var racers: LinkedHashMap<UUID, Racer>
    var eliminated: LinkedHashMap<UUID, ElimReason>
    var alive: LinkedHashSet<UUID>

}
