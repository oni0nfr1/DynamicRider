package io.github.oni0nfr1.dynamicrider.client.rider.v2.actionbar

import io.github.oni0nfr1.dynamicrider.client.event.RiderActionBarCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult

object KartSpeedometer {
    val speedRegex = Regex("""(\d{1,4})(?:\.(\d))?\s*km\s*/?\s*h""", RegexOption.IGNORE_CASE)

    private val eventListener = RiderActionBarCallback.EVENT.register { _, raw ->
        updateSpeed(raw)
        HandleResult.PASS
    }

    var speed: Int = 0

    fun updateSpeed(raw: String) {
        val match = speedRegex.find(raw) ?: return
        val readSpeed = match.groupValues[1].toInt()
        speed = readSpeed
    }
}
