package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.state.RaceState

class PreviewRaceState : RaceState {
    override var racing: Boolean = true
    override var elapsedTimeMillis: Long = 42_000L
    override var currentLap: Int = 2
    override var maxLap: Int? = 3
    override var bestLapTimeMillis: Long? = 58_000L
}
