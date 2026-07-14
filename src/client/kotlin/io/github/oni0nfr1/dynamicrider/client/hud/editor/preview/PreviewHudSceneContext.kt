package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType
import io.github.oni0nfr1.dynamicrider.client.hud.state.RankingState

class PreviewHudSceneContext<S : KartState>(
    override val kartStateType: KartStateType<S>,
    override val kartState: S,
    override val raceState: PreviewRaceState = PreviewRaceState(),
    val previewRankingState: PreviewRankingState = PreviewRankingState(),
    override val clock: PreviewHudClock = PreviewHudClock(),
) : HudSceneContext<S> {
    override val rankingState: RankingState
        get() = previewRankingState.value
}
