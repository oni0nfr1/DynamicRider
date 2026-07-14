package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes

/** 상태 타입에 맞는 기본 편집기 preview scene context를 생성한다. */
object PreviewHudSceneContextFactory {
    /**
     * [stateType]에 대응하는 가변 preview 상태와 기본 scene 상태를 조립한다.
     *
     * 등록되지 않은 별도 상태 타입은 기본 preview 구현을 알 수 없으므로 예외를 던진다.
     */
    fun create(
        stateType: KartStateType<out KartState>,
    ): PreviewHudSceneContext<out KartState> = when (stateType) {
        KartStateTypes.X -> context(KartStateTypes.X, DefaultPreviewXKartState())
        KartStateTypes.EX -> context(KartStateTypes.EX, DefaultPreviewEXKartState())
        KartStateTypes.JIU -> context(KartStateTypes.JIU, DefaultPreviewJiuKartState())
        KartStateTypes.NEW -> context(KartStateTypes.NEW, DefaultPreviewNewKartState())
        KartStateTypes.Z7 -> context(KartStateTypes.Z7, DefaultPreviewZ7KartState())
        KartStateTypes.V1 -> context(KartStateTypes.V1, DefaultPreviewV1KartState())
        KartStateTypes.A2 -> context(KartStateTypes.A2, DefaultPreviewA2KartState())
        KartStateTypes.LEGACY -> context(KartStateTypes.LEGACY, DefaultPreviewLegacyKartState())
        KartStateTypes.PRO -> context(KartStateTypes.PRO, DefaultPreviewProKartState())
        KartStateTypes.RUSHPLUS -> context(KartStateTypes.RUSHPLUS, DefaultPreviewRushPlusKartState())
        KartStateTypes.CHARGE -> context(KartStateTypes.CHARGE, DefaultPreviewChargeKartState())
        KartStateTypes.SR -> context(KartStateTypes.SR, DefaultPreviewSRKartState())
        KartStateTypes.N1 -> context(KartStateTypes.N1, DefaultPreviewN1KartState())
        KartStateTypes.RX -> context(KartStateTypes.RX, DefaultPreviewRXKartState())
        KartStateTypes.KEY -> context(KartStateTypes.KEY, DefaultPreviewKeyKartState())
        KartStateTypes.GEAR -> context(KartStateTypes.GEAR, DefaultPreviewGearKartState())
        KartStateTypes.F1 -> context(KartStateTypes.F1, DefaultPreviewF1KartState())
        KartStateTypes.RALLY -> context(KartStateTypes.RALLY, DefaultPreviewRallyKartState())
        KartStateTypes.MK -> context(KartStateTypes.MK, DefaultPreviewMKKartState())
        KartStateTypes.BOAT -> context(KartStateTypes.BOAT, DefaultPreviewBoatKartState())
        else -> throw IllegalArgumentException("Unsupported preview kart state type: ${stateType.id}")
    }

    private fun <S : KartState> context(
        stateType: KartStateType<S>,
        state: S,
    ): PreviewHudSceneContext<S> = PreviewHudSceneContext(
        kartStateType = stateType,
        kartState = state,
    )
}
