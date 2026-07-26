package io.github.oni0nfr1.dynamicrider.client.hud.runtime.state

import io.github.oni0nfr1.dynamicrider.client.hud.state.RaceState
import io.github.oni0nfr1.dynamicrider.client.rider.backend.race.KartLapTracker
import io.github.oni0nfr1.dynamicrider.client.rider.backend.race.KartRaceTimer

class LiveRaceState(
    private val currentLapProvider: () -> Int?,
    private val maxLapProvider: () -> Int?,
) : RaceState {
    override val racing: Boolean
        get() = KartRaceTimer.isRacing
    override val elapsedTimeMillis: Long
        get() = KartRaceTimer.time.interpolatedTotalMillis
    override val currentLap: Int
        get() = currentLapProvider() ?: 0
    override val maxLap: Int?
        get() = maxLapProvider() ?: KartLapTracker.maxLap
    override val bestLapTimeMillis: Long?
        get() = KartLapTracker.bestLapTime
}
