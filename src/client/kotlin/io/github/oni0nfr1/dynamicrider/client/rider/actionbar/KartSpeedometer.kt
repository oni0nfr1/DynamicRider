package io.github.oni0nfr1.dynamicrider.client.rider.actionbar

import io.github.oni0nfr1.dynamicrider.client.event.RiderActionBarCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.state.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.rider.RiderBackend

class KartSpeedometer(
    override val stateManager: HudStateManager,
): RiderBackend, AutoCloseable {

    companion object {
        val speedRegex = Regex("""(\d{1,4})(?:\.(\d))?\s*km\s*/?\s*h""", RegexOption.IGNORE_CASE)
    }

    private val eventListener = RiderActionBarCallback.EVENT.register { _, raw ->
        updateSpeed(raw)
        HandleResult.PASS
    }

    val speed = mutableStateOf(stateManager, 0)

    fun updateSpeed(raw: String) {
        val match = speedRegex.find(raw) ?: return
        val readSpeed = match.groupValues[1].toInt()
        speed.set(readSpeed)
    }

    override fun close() {
        eventListener.close()
    }
}