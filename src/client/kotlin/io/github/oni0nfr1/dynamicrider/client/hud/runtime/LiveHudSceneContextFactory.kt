package io.github.oni0nfr1.dynamicrider.client.hud.runtime

import io.github.oni0nfr1.dynamicrider.client.hud.runtime.state.*
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.state.*
import io.github.oni0nfr1.skid.client.api.engine.*
import io.github.oni0nfr1.skid.client.api.kart.KartRef

/** Skid 엔진을 HUD가 사용하는 live scene context로 변환하는 유일한 진입점이다. */
object LiveHudSceneContextFactory {
    fun create(kart: KartRef): HudSceneContext<KartState>? = kart.access {
        engine?.let(::create)
    }

    fun create(engine: KartEngine): HudSceneContext<KartState> = when (engine) {
        is XEngine -> context(engine, KartStateTypes.X, ::LiveXKartState)
        is EXEngine -> context(engine, KartStateTypes.EX, ::LiveEXKartState)
        is JiuEngine -> context(engine, KartStateTypes.JIU, ::LiveJiuKartState)
        is NewEngine -> context(engine, KartStateTypes.NEW, ::LiveNewKartState)
        is Z7Engine -> context(engine, KartStateTypes.Z7, ::LiveZ7KartState)
        is V1Engine -> context(engine, KartStateTypes.V1, ::LiveV1KartState)
        is A2Engine -> context(engine, KartStateTypes.A2, ::LiveA2KartState)
        is LegacyEngine -> context(engine, KartStateTypes.LEGACY, ::LiveLegacyKartState)
        is ProEngine -> context(engine, KartStateTypes.PRO, ::LiveProKartState)
        is RushPlusEngine -> context(engine, KartStateTypes.RUSHPLUS, ::LiveRushPlusKartState)
        is ChargeEngine -> context(engine, KartStateTypes.CHARGE, ::LiveChargeKartState)
        is SREngine -> context(engine, KartStateTypes.SR, ::LiveSRKartState)
        is N1Engine -> context(engine, KartStateTypes.N1, ::LiveN1KartState)
        is RXEngine -> context(engine, KartStateTypes.RX, ::LiveRXKartState)
        is KeyEngine -> context(engine, KartStateTypes.KEY, ::LiveKeyKartState)
        is GearEngine -> context(engine, KartStateTypes.GEAR, ::LiveGearKartState)
        is F1Engine -> context(engine, KartStateTypes.F1, ::LiveF1KartState)
        is RallyEngine -> context(engine, KartStateTypes.RALLY, ::LiveRallyKartState)
        is MKEngine -> context(engine, KartStateTypes.MK, ::LiveMKKartState)
        is BoatEngine -> context(engine, KartStateTypes.BOAT) { LiveBoatKartState() }
    }

    private fun <E : KartEngine, S : KartState> context(
        engine: E,
        stateType: KartStateType<S>,
        stateFactory: (KartRef.Specific<E>) -> S,
    ): HudSceneContext<S> {
        val kart = specific(engine)
        return context(kart, stateType, stateFactory(kart))
    }

    private fun <E : KartEngine, S : KartState> context(
        kart: KartRef.Specific<E>,
        stateType: KartStateType<S>,
        state: S,
    ): HudSceneContext<S> = LiveHudSceneContext(
        kartStateType = stateType,
        kartState = state,
        raceState = LiveRaceState {
            kart.accessEngine { it.currentLap } ?: 0
        },
    )

    private fun <E : KartEngine> specific(engine: E): KartRef.Specific<E> {
        return KartRef.Specific(engine, engine.javaClass)
    }
}
