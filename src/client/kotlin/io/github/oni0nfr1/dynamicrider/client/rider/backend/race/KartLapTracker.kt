package io.github.oni0nfr1.dynamicrider.client.rider.backend.race

import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient
import io.github.oni0nfr1.dynamicrider.client.event.RiderLapFinishCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.time.Millis
import io.github.oni0nfr1.dynamicrider.client.rider.backend.RiderBackend
import io.github.oni0nfr1.dynamicrider.client.util.debugLog
import io.github.oni0nfr1.skid.client.api.events.unstable.KartAttrModifierEvents
import io.github.oni0nfr1.skid.client.api.kart.ridingKart
import io.github.oni0nfr1.skid.client.api.kart.subject
import io.github.oni0nfr1.skid.client.api.kart.unstable.maxLap
import io.github.oni0nfr1.skid.client.api.utils.access
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player

object KartLapTracker : RiderBackend() {

    override fun onRaceStart() {
        raceActive = true
        currentLap = 1
        maxLap = (Minecraft.getInstance().player?.subject as? Player)?.ridingKart?.access { maxLap } ?: 0
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

        KartAttrModifierEvents.CTX_MAX_LAP.register { kartEntity, _, value ->
            val rider = Minecraft.getInstance().player ?: return@register
            if (!raceActive || rider.vehicle !== kartEntity) return@register

            maxLap = value.toInt()
        }
    }

    private fun isRaceActiveNow(): Boolean {
        return try {
            DynamicRiderClient.instance.raceActive
        } catch (_: IllegalStateException) {
            false
        }
    }
}
