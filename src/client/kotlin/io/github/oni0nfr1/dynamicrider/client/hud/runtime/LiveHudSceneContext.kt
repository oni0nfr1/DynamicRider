package io.github.oni0nfr1.dynamicrider.client.hud.runtime

import io.github.oni0nfr1.dynamicrider.client.hud.runtime.state.LiveRankingState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudClock
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType
import io.github.oni0nfr1.dynamicrider.client.hud.state.RaceState
import io.github.oni0nfr1.dynamicrider.client.hud.state.RankingState

class LiveHudSceneContext<S : KartState>(
    override val kartStateType: KartStateType<S>,
    override val kartState: S,
    override val raceState: RaceState,
    private val liveRankingState: LiveRankingState = LiveRankingState(),
    override val clock: HudClock = HudClock(System::currentTimeMillis),
) : HudSceneContext<S> {
    override val rankingState: RankingState
        get() = liveRankingState.current()
}
