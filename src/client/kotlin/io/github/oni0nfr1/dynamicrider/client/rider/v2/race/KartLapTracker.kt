package io.github.oni0nfr1.dynamicrider.client.rider.v2.race

import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient
import io.github.oni0nfr1.dynamicrider.client.event.RiderLapFinishCallback
import io.github.oni0nfr1.dynamicrider.client.event.attribute.RiderAttrCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.Millis
import io.github.oni0nfr1.dynamicrider.client.rider.v2.RiderBackend
import io.github.oni0nfr1.dynamicrider.client.util.isClientPlayerId

object KartLapTracker : RiderBackend() {

    override fun onRaceStart() {
        raceActive = true
        currentLap = 1
        maxLap = RiderAttrCallback.MAX_LAP.myValue?.toInt()
        bestLapTime = null
        lapTimes = emptyList()
    }

    override fun onRaceEnd() {
        raceActive = false
        currentLap = 1
        maxLap = null
        bestLapTime = null
        lapTimes = emptyList()
    }

    private var raceActive: Boolean = isRaceActiveNow()

    var currentLap: Int = 1
        private set

    var maxLap: Int? = null
        private set

    var bestLapTime: Millis? = null
        private set

    var lapTimes: List<Millis> = emptyList()
        private set

    override fun init() {
        if (raceActive) {
            onRaceStart()
        }

        RiderLapFinishCallback.EVENT.register { msg ->
            if (!raceActive) return@register HandleResult.PASS

            val currentBestLapTime = bestLapTime ?: Long.MAX_VALUE
            maxLap = msg.maxLap
            currentLap = msg.currentLap + 1
            if (msg.timeMillis < currentBestLapTime) {
                bestLapTime = msg.timeMillis
            }
            lapTimes = lapTimes + msg.timeMillis
            HandleResult.PASS
        }

        RiderAttrCallback.MAX_LAP.register { entityId, value ->
            if (!raceActive || !isClientPlayerId(entityId)) {
                return@register HandleResult.PASS
            }

            maxLap = value.toInt()
            HandleResult.PASS
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
