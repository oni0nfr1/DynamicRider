package io.github.oni0nfr1.dynamicrider.client.rider.backend.race

import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient
import io.github.oni0nfr1.dynamicrider.client.event.RiderLapFinishCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.time.Millis
import io.github.oni0nfr1.dynamicrider.client.rider.backend.RiderBackend
import io.github.oni0nfr1.dynamicrider.client.util.debugLog
import io.github.oni0nfr1.dynamicrider.client.util.isClientPlayerId
import io.github.oni0nfr1.skid.client.api.attr.maxLap
import io.github.oni0nfr1.skid.client.api.events.RiderAttrEvents
import net.minecraft.client.Minecraft

object KartLapTracker : RiderBackend() {

    override fun onRaceStart() {
        raceActive = true
        currentLap = 1
        maxLap = Minecraft.getInstance().level?.maxLap ?: 0
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
            debugLog("${msg.timeMillis} ${msg.currentLap} ${msg.maxLap}")

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

        RiderAttrEvents.MAX_LAP.register { entity, value ->
            if (!raceActive || !isClientPlayerId(entity.id)) return@register

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
