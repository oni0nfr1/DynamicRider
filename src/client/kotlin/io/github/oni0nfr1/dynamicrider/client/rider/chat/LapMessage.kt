package io.github.oni0nfr1.dynamicrider.client.rider.chat

import io.github.oni0nfr1.dynamicrider.client.rider.Millis
import net.minecraft.network.chat.Component
import net.minecraft.world.scores.Scoreboard

data class LapMessage(
    val currentLap: Int,
    val maxLap: Int,
    val timeMillis: Millis
) {
    companion object {
        @JvmStatic
        @Suppress("UNUSED_PARAMETER")
        fun parseLapMessage(component: Component, scoreboard: Scoreboard): LapMessage? {
            val raw = component.string.trim()
            val match = LAP_REGEX.matchEntire(raw) ?: return null

            val currentLap = match.groupValues[1].toIntOrNull() ?: return null
            val maxLap = match.groupValues[2].toIntOrNull() ?: return null
            val minutes = match.groupValues[3].toIntOrNull() ?: return null
            val seconds = match.groupValues[4].toIntOrNull() ?: return null
            val millis = match.groupValues[5].toIntOrNull() ?: return null

            if (seconds !in 0..59) return null
            if (millis !in 0..999) return null

            val timeMillis = minutes.toLong() * 60_000L + seconds.toLong() * 1_000L + millis.toLong()
            return LapMessage(currentLap, maxLap, timeMillis)
        }

        private val LAP_REGEX =
            Regex("""\s*(\d+)\s*/\s*(\d+)\s*\|\s*(\d{1,3}):(\d{2})\.(\d{3})\s*""")
    }
}
