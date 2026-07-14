package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.state.HudClock

class PreviewHudClock(
    initialTimeMillis: Long = 0L,
) : HudClock {
    private var timeMillis: Long = initialTimeMillis

    var paused: Boolean = false
        private set

    override fun currentTimeMillis(): Long = timeMillis

    fun advance(deltaMillis: Long) {
        if (!paused) timeMillis = (timeMillis + deltaMillis).coerceAtLeast(0L)
    }

    fun seek(timeMillis: Long) {
        this.timeMillis = timeMillis.coerceAtLeast(0L)
    }

    fun pause() {
        paused = true
    }

    fun resume() {
        paused = false
    }
}
