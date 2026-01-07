package io.github.oni0nfr1.dynamicrider.client.rider.actionbar

import io.github.oni0nfr1.dynamicrider.client.event.RiderTachometerCallback
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.state.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.rider.RiderBackend
import net.minecraft.world.InteractionResult

class KartSpeedometer(
    override val stateManager: HudStateManager,
): RiderBackend, AutoCloseable {

    companion object {
        val speedRegex = Regex("""(\d{1,4})(?:\.(\d))?\s*km\s*/?\s*h""", RegexOption.IGNORE_CASE)
    }

    private val eventListener = RiderTachometerCallback.EVENT.register { _, _, raw ->
        updateSpeed(raw)
        InteractionResult.PASS
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