package io.github.oni0nfr1.dynamicrider.client.rider.v2.race

import io.github.oni0nfr1.dynamicrider.client.event.RiderLapFinishCallback
import io.github.oni0nfr1.dynamicrider.client.event.attribute.RiderAttrCallback
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderRaceEndCallback
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderRaceStartCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.Millis
import io.github.oni0nfr1.dynamicrider.client.util.isClientPlayerId

object KartLapTracker {
    private val raceStartListener = RiderRaceStartCallback.EVENT.register {
        raceActive = true
        resetForRaceStart()
        HandleResult.PASS
    }

    private val raceEndListener = RiderRaceEndCallback.EVENT.register { _ ->
        raceActive = false
        resetForRaceEnd()
        HandleResult.PASS
    }

    private val lapFinishListener = RiderLapFinishCallback.EVENT.register { msg ->
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

    private val maxLapListener = RiderAttrCallback.MAX_LAP.register { entityId, value ->
        if (!raceActive || !isClientPlayerId(entityId)) {
            return@register HandleResult.PASS
        }

        maxLap = value.toInt()
        HandleResult.PASS
    }

    private var raceActive: Boolean = false

    var currentLap: Int = 1
        private set

    var maxLap: Int? = null
        private set

    var bestLapTime: Millis? = null
        private set

    var lapTimes: List<Millis> = emptyList()
        private set

    private fun resetForRaceStart() {
        currentLap = 1
        maxLap = RiderAttrCallback.MAX_LAP.myValue?.toInt()
        bestLapTime = null
        lapTimes = emptyList()
    }

    private fun resetForRaceEnd() {
        currentLap = 1
        maxLap = null
        bestLapTime = null
        lapTimes = emptyList()
    }
}
