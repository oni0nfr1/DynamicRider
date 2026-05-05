package io.github.oni0nfr1.dynamicrider.client.rider.backend

import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderRaceEndCallback
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderRaceStartCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.backend.bossbar.KartTeamBoostTracker
import io.github.oni0nfr1.dynamicrider.client.rider.backend.exp.KartExpProgressReader
import io.github.oni0nfr1.dynamicrider.client.rider.backend.race.KartLapTracker
import io.github.oni0nfr1.dynamicrider.client.rider.backend.race.KartRaceTimer
import io.github.oni0nfr1.dynamicrider.client.rider.backend.sidebar.KartRankingManager
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