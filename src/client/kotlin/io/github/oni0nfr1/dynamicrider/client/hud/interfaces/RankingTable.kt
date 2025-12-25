package io.github.oni0nfr1.dynamicrider.client.hud.interfaces

import net.minecraft.world.entity.player.Player

interface RankingTable: HudElement {

    val players: List<Player>
    val racingPlayers: List<Player>
    val spectators: List<Player>

}