package io.github.oni0nfr1.dynamicrider.client.rider

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import java.lang.System.currentTimeMillis

typealias Millis = Long

class RaceTime {
    var rawTotalMillis: Millis = 0
    private var lastUpdatedSystemTime: Millis = 0

    fun updateFromSidebar(raw: String) {
        if (raw.length < 7) return

        try {
            var minutes: Int
            var seconds: Int
            var milliseconds: Int

            val colonIdx = raw.indexOf(':')
            val dotIdx = raw.indexOf('.')

            if (colonIdx != -1 && dotIdx != -1) {
                minutes = parsePosInt(raw, 0, colonIdx)
                seconds = parsePosInt(raw, colonIdx + 1, dotIdx)
                milliseconds = parsePosInt(raw, dotIdx + 1, raw.length)

                this.rawTotalMillis = (minutes * 60000L) + (seconds * 1000L) + milliseconds
                this.lastUpdatedSystemTime = currentTimeMillis()
            }
        } catch (e: Exception) {
            ResourceStore.logger.warn("RaceTime: Parse Failed", e)
        }
    }

    private fun parsePosInt(src: String, start: Int, end: Int): Int {
        var res = 0
        for (i in start until end) {
            val c = src[i]
            if (c in '0'..'9') {
                res = res * 10 + (c - '0')
            }
        }
        return res
    }

    val interpolatedTotalMillis: Millis
        get() {
            if (lastUpdatedSystemTime == 0L) return rawTotalMillis
            val delta = currentTimeMillis() - lastUpdatedSystemTime
            return rawTotalMillis + delta
        }
}