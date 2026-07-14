package io.github.oni0nfr1.dynamicrider.client.hud.runtime.state

import io.github.oni0nfr1.dynamicrider.client.hud.state.RaceState
import io.github.oni0nfr1.dynamicrider.client.rider.backend.race.KartLapTracker
import io.github.oni0nfr1.dynamicrider.client.rider.backend.race.KartRaceTimer
import io.github.oni0nfr1.skid.client.api.attr.maxLap
import net.minecraft.client.Minecraft

class LiveRaceState(
    private val currentLapProvider: () -> Int,
) : RaceState {
    override val racing: Boolean
        get() = KartRaceTimer.isRacing
    override val elapsedTimeMillis: Long
        get() = KartRaceTimer.time.interpolatedTotalMillis
    override val currentLap: Int
        get() = currentLapProvider()
    override val maxLap: Int?
        get() = Minecraft.getInstance().level?.maxLap ?: KartLapTracker.maxLap
    override val bestLapTimeMillis: Long?
        get() = KartLapTracker.bestLapTime
}
