package io.github.oni0nfr1.dynamicrider.client.rider.v2.race

import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderTimerUpdateCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.RaceTime
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.SidebarSnapshot
import io.github.oni0nfr1.dynamicrider.client.rider.v2.RiderBackend

object KartRaceTimer : RiderBackend() {

    private var raceActive: Boolean = isRaceActiveNow()

    var time: RaceTime = RaceTime()
        private set

    var isRacing: Boolean = false
        private set

    override fun init() {
        if (raceActive) {
            bootstrapCurrentTimer()
        }

        RiderTimerUpdateCallback.EVENT.register { _, raw ->
            if (!raceActive) return@register HandleResult.PASS

            time.updateFromSidebar(raw)
            if (time.rawTotalMillis != 0L) {
                isRacing = true
            }
            HandleResult.PASS
        }
    }

    override fun onRaceStart() {
        raceActive = true
        time = RaceTime()
        isRacing = false
    }

    override fun onRaceEnd() {
        raceActive = false
        time = RaceTime()
        isRacing = false
    }

    private fun bootstrapCurrentTimer() {
        val sidebarTitle = SidebarSnapshot.fromMcClient()?.title?.string ?: return
        time.updateFromSidebar(sidebarTitle)
        if (time.rawTotalMillis != 0L) {
            isRacing = true
        }
    }

    private fun isRaceActiveNow(): Boolean {
        return try {
            DynamicRiderClient.instance.raceSession != null
        } catch (_: IllegalStateException) {
            false
        }
    }
}
