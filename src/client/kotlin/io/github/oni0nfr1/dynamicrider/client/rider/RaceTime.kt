package io.github.oni0nfr1.dynamicrider.client.rider

import net.minecraft.network.chat.Component
import java.lang.System.currentTimeMillis

data class RaceTime(
    var minutes: Int = 0,
    var seconds: Int = 0,
    var milliseconds: Int = 0,
) {
    companion object {
        val sidebarRegex = Regex("""^(\d{1,2}):([0-5]\d)\.(\d{3})$""")
    }

    private var lastUpdated: Long = currentTimeMillis()
    val updateTimeDelta: Long
        get() = currentTimeMillis() - lastUpdated

    var totalMillis: Int
        get() = minutes * 60000 + seconds * 1000 + milliseconds
        set(value) {
            minutes =  value / 60000
            seconds = (value % 60000) / 1000
            milliseconds = value % 1000
        }

    fun update(
        component: Component,
        regex: Regex = sidebarRegex
    ) {
        val raw = component.string
        val match = regex.matchEntire(raw)
            ?: throw IllegalArgumentException("Invalid time format: $raw")

        minutes = match.groupValues[1].toInt()
        seconds = match.groupValues[2].toInt()
        milliseconds = match.groupValues[3].toInt()
        lastUpdated = currentTimeMillis()
    }

    override fun toString(): String {
        return "$minutes:$seconds.$milliseconds"
    }

    fun toInterpolatedString(): String {
        val dt = updateTimeDelta
        val interpolated = totalMillis + dt
        val min =  interpolated / 60000
        val sec = (interpolated % 60000) / 1000
        val ms =   interpolated % 1000

        return "$min:$sec.$ms"
    }
}