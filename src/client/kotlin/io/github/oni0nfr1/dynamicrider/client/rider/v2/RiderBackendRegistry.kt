package io.github.oni0nfr1.dynamicrider.client.rider.v2

import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderRaceEndCallback
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderRaceStartCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.v2.bossbar.KartTeamBoostTracker
import io.github.oni0nfr1.dynamicrider.client.rider.v2.exp.KartExpProgressReader
import io.github.oni0nfr1.dynamicrider.client.rider.v2.race.KartLapTracker
import io.github.oni0nfr1.dynamicrider.client.rider.v2.race.KartRaceTimer
import io.github.oni0nfr1.dynamicrider.client.rider.v2.sidebar.KartRankingManager
import io.github.oni0nfr1.skid.client.api.events.KartMountEvents

object RiderBackendRegistry {
    private val modules: List<RiderBackend> = listOf(
        KartTeamBoostTracker,
        KartExpProgressReader,
        KartLapTracker,
        KartRaceTimer,
        KartRankingManager,
    )

    fun init() {
        modules.forEach { it.bootstrap() }

        RiderRaceStartCallback.EVENT.register {
            modules.forEach { it.onRaceStart() }
            HandleResult.PASS
        }
        RiderRaceEndCallback.EVENT.register {
            modules.forEach { it.onRaceEnd() }
            HandleResult.PASS
        }
        KartMountEvents.DISMOUNT.register { kartEntity, rider ->
            modules.forEach { it.onRiderDismount(kartEntity, rider) }
        }
        KartMountEvents.MOUNT.register { kartEntity, rider ->
            modules.forEach { it.onRiderMount(kartEntity, rider) }
        }
    }
}