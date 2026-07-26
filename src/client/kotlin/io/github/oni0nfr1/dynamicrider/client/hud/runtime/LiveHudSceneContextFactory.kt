package io.github.oni0nfr1.dynamicrider.client.hud.runtime

import io.github.oni0nfr1.dynamicrider.client.hud.runtime.state.*
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.state.*
import io.github.oni0nfr1.skid.client.api.engine.*
import io.github.oni0nfr1.skid.client.api.kart.Kart
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import io.github.oni0nfr1.skid.client.api.kart.unstable.currentLap
import io.github.oni0nfr1.skid.client.api.kart.unstable.maxLap
import io.github.oni0nfr1.skid.client.api.utils.KartType
import io.github.oni0nfr1.skid.client.api.utils.Ref
import io.github.oni0nfr1.skid.client.api.utils.access

/** Skid 엔진을 HUD가 사용하는 live scene context로 변환하는 유일한 진입점이다. */
object LiveHudSceneContextFactory {
    fun create(kart: KartRef): HudSceneContext<KartState>? = kart.access {
        when (type) {
            KartType.X -> context(kart.specify(KartType.X), KartStateTypes.X, ::LiveXKartState)
            KartType.EX -> context(kart.specify(KartType.EX), KartStateTypes.EX, ::LiveEXKartState)
            KartType.JIU -> context(kart.specify(KartType.JIU), KartStateTypes.JIU, ::LiveJiuKartState)
            KartType.NEW -> context(kart.specify(KartType.NEW), KartStateTypes.NEW, ::LiveNewKartState)
            KartType.Z7 -> context(kart.specify(KartType.Z7), KartStateTypes.Z7, ::LiveZ7KartState)
            KartType.V1 -> context(kart.specify(KartType.V1), KartStateTypes.V1, ::LiveV1KartState)
            KartType.A2 -> context(kart.specify(KartType.A2), KartStateTypes.A2, ::LiveA2KartState)
            KartType.LEGACY -> context(kart.specify(KartType.LEGACY), KartStateTypes.LEGACY, ::LiveLegacyKartState)
            KartType.PRO -> context(kart.specify(KartType.PRO), KartStateTypes.PRO, ::LiveProKartState)
            KartType.RUSHPLUS -> context(kart.specify(KartType.RUSHPLUS), KartStateTypes.RUSHPLUS, ::LiveRushPlusKartState)
            KartType.CHARGE -> context(kart.specify(KartType.CHARGE), KartStateTypes.CHARGE, ::LiveChargeKartState)
            KartType.SR -> context(kart.specify(KartType.SR), KartStateTypes.SR, ::LiveSRKartState)
            KartType.N1 -> context(kart.specify(KartType.N1), KartStateTypes.N1, ::LiveN1KartState)
            KartType.RX -> context(kart.specify(KartType.RX), KartStateTypes.RX, ::LiveRXKartState)
            KartType.KEY -> context(kart.specify(KartType.KEY), KartStateTypes.KEY, ::LiveKeyKartState)
            KartType.MK -> context(kart.specify(KartType.MK), KartStateTypes.MK, ::LiveMKKartState)
            KartType.BOAT -> context(kart.specify(KartType.BOAT), KartStateTypes.BOAT) { LiveBoatKartState() }
            KartType.GEAR -> context(kart.specify(KartType.GEAR), KartStateTypes.GEAR, ::LiveGearKartState)
            KartType.F1 -> context(kart.specify(KartType.F1), KartStateTypes.F1, ::LiveF1KartState)
            KartType.RALLY -> context(kart.specify(KartType.RALLY), KartStateTypes.RALLY, ::LiveRallyKartState)
            KartType.DS -> context(kart.specify(KartType.DS), KartStateTypes.DS, ::LiveDSKartState)
        }
    }

    private fun <E : KartEngine, S : KartState> context(
        kart: Ref<Kart<E>>,
        stateType: KartStateType<S>,
        stateFactory: (Ref<Kart<E>>) -> S,
    ): HudSceneContext<S> {
        return context(kart, stateType, stateFactory(kart))
    }


    private fun <E : KartEngine, S : KartState> context(
        kart: Ref<Kart<E>>,
        stateType: KartStateType<S>,
        state: S,
    ): HudSceneContext<S> = LiveHudSceneContext(
        kartStateType = stateType,
        kartState = state,
        raceState = LiveRaceState(
            currentLapProvider = {
                kart.access { currentLap }
            },
            maxLapProvider = {
                kart.access { maxLap }
            },
        ),
    )

}
