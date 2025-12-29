package io.github.oni0nfr1.dynamicrider.client.hud.interfaces

import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.RankingManager
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.RankingManager.ElimReason
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.RankingManager.Racer
import java.util.UUID

interface RankingTable: HudElement {

    var ranking: List<RankingManager.RankingEntry>
    var racers: LinkedHashMap<UUID, Racer>
    var eliminated: LinkedHashMap<UUID, ElimReason>
    var alive: LinkedHashSet<UUID>

}