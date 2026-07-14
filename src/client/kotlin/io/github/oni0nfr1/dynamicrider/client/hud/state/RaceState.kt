package io.github.oni0nfr1.dynamicrider.client.hud.state

/** 엔진 종류와 무관한 현재 경기 진행 상태다. */
interface RaceState {
    val racing: Boolean
    val elapsedTimeMillis: Long
    val currentLap: Int
    val maxLap: Int?
    val bestLapTimeMillis: Long?
}
