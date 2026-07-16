package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes

/** 현재 클라이언트 세션에서 마지막으로 연 HUD 장면을 기억한다. */
internal object HudEditorSceneSelection {
    var mode: HudSceneMode = HudSceneMode.RIDE
        private set

    var stateType: KartStateType<out KartState> = KartStateTypes.JIU
        private set

    fun remember(mode: HudSceneMode, stateType: KartStateType<out KartState>) {
        this.mode = mode
        this.stateType = stateType
    }
}
