package io.github.oni0nfr1.dynamicrider.client.rider.sidebar

import io.github.oni0nfr1.dynamicrider.client.event.RiderTimerUpdateCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.state.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.state.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.rider.RaceTime
import io.github.oni0nfr1.dynamicrider.client.rider.RiderBackend

class RaceTimeParser(
    override val stateManager: HudStateManager
): RiderBackend, AutoCloseable {

    val time = MutableState(stateManager, RaceTime())
    val isRacing = mutableStateOf(stateManager, false)

    val timeUpdateListener = RiderTimerUpdateCallback.EVENT.register { _, raw ->
        time.mutate { updateFromSidebar(raw) }
        if (time.silentRead().rawTotalMillis != 0L) {
            isRacing.set(true)
        }
        HandleResult.PASS
    }

    override fun close() {
        timeUpdateListener.close()
    }
}