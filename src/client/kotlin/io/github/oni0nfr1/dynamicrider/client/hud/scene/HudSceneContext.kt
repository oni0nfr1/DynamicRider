package io.github.oni0nfr1.dynamicrider.client.hud.scene

import io.github.oni0nfr1.dynamicrider.client.hud.state.HudClock
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType
import io.github.oni0nfr1.dynamicrider.client.hud.state.RaceState
import io.github.oni0nfr1.dynamicrider.client.hud.state.RankingState

/** HUD 장면과 모든 요소에 전달되는 읽기 전용 실행 환경이다. */
interface HudSceneContext<out S : KartState> {
    val kartStateType: KartStateType<out S>
    val kartState: S
    val raceState: RaceState
    val rankingState: RankingState
    val clock: HudClock

    fun nanoTime(): Long = clock.currentTimeMillis() * 1_000_000L
}
