package io.github.oni0nfr1.dynamicrider.client.rider.v2.race

import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderRaceEndCallback
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderRaceStartCallback
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderTimerUpdateCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.RaceTime

object KartRaceTimer {
    private val raceStartListener = RiderRaceStartCallback.EVENT.register {
        raceActive = true
        reset()
        HandleResult.PASS
    }

    private val raceEndListener = RiderRaceEndCallback.EVENT.register { _ ->
        raceActive = false
        reset()
        HandleResult.PASS
    }

    private val timeUpdateListener = RiderTimerUpdateCallback.EVENT.register { _, raw ->
        if (!raceActive) return@register HandleResult.PASS

        time.updateFromSidebar(raw)
        if (time.rawTotalMillis != 0L) {
            isRacing = true
        }
        HandleResult.PASS
    }

    private var raceActive: Boolean = false

    var time: RaceTime = RaceTime()
        private set

    var isRacing: Boolean = false
        private set

    private fun reset() {
        time = RaceTime()
        isRacing = false
    }
}
