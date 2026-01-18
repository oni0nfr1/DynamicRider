package io.github.oni0nfr1.dynamicrider.client.rider.chat

import io.github.oni0nfr1.dynamicrider.client.event.RiderLapFinishCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.state.MutableState
import io.github.oni0nfr1.dynamicrider.client.rider.Millis
import io.github.oni0nfr1.dynamicrider.client.rider.RiderBackend

class KartLapTimeManager(
    override val stateManager: HudStateManager
): RiderBackend, AutoCloseable {

    val maxLap = MutableState<Int?>(stateManager, null)
    val currentLap = MutableState(stateManager, 1)
    val bestTime = MutableState(stateManager, Long.MAX_VALUE)
    val lapTimes = MutableState(stateManager, mutableListOf<Millis>())

    val lapTimeListener = RiderLapFinishCallback.EVENT.register { msg ->
        maxLap.set(msg.maxLap)
        currentLap.set(msg.currentLap + 1)
        if (msg.timeMillis < bestTime.silentRead()) bestTime.set(msg.timeMillis)
        lapTimes.mutate { addLast(msg.timeMillis) }
        HandleResult.PASS
    }

    override fun close() {
        lapTimeListener.close()
    }
}