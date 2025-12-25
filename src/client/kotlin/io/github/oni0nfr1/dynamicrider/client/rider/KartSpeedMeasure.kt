package io.github.oni0nfr1.dynamicrider.client.rider

import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.mutableStateOf

object KartSpeedMeasure {

    var enabled: Boolean = false
    val speedRegex = Regex("""(\d{1,4})(?:\.(\d))?\s*km\s*/?\s*h""", RegexOption.IGNORE_CASE)

    lateinit var stateManager: HudStateManager
    lateinit var speed: MutableState<Int>

    fun init(stateManager: HudStateManager) {
        this.stateManager = stateManager
        this.speed = mutableStateOf(stateManager, 0)
    }

    @JvmStatic
    fun updateSpeed(raw: String) {
        if (!enabled) return

        val match = speedRegex.find(raw) ?: return
        val readSpeed = match.groupValues[1].toInt()
        speed.set(readSpeed)
    }

}