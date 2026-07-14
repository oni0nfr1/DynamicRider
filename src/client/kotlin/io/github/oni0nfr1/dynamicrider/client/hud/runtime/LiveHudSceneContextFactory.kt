package io.github.oni0nfr1.dynamicrider.client.hud.runtime

import io.github.oni0nfr1.dynamicrider.client.hud.runtime.state.LiveChargeKartState
import io.github.oni0nfr1.dynamicrider.client.hud.runtime.state.LiveDraftKartState
import io.github.oni0nfr1.dynamicrider.client.hud.runtime.state.LiveDraftSpeedKartState
import io.github.oni0nfr1.dynamicrider.client.hud.runtime.state.LiveJiuKartState
import io.github.oni0nfr1.dynamicrider.client.hud.runtime.state.LiveKartState
import io.github.oni0nfr1.dynamicrider.client.hud.runtime.state.LiveNitroDraftKartState
import io.github.oni0nfr1.dynamicrider.client.hud.runtime.state.LiveNitroKartState
import io.github.oni0nfr1.dynamicrider.client.hud.runtime.state.LiveRaceState
import io.github.oni0nfr1.dynamicrider.client.hud.runtime.state.LiveV1KartState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.state.ChargeKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.DraftKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.DraftSpeedKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.JiuKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroDraftKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.V1KartState
import io.github.oni0nfr1.skid.client.api.engine.*
import io.github.oni0nfr1.skid.client.api.kart.KartRef

/** Skid 엔진을 HUD가 사용하는 live scene context로 변환하는 유일한 진입점이다. */
object LiveHudSceneContextFactory {
    fun create(kart: KartRef): HudSceneContext<out KartState>? = kart.access {
        engine?.let(::create)
    }

    fun create(engine: KartEngine): HudSceneContext<out KartState> = when (engine) {
        is XEngine -> nitroDraft(engine, KartStateTypes.X)
        is EXEngine -> nitroDraft(engine, KartStateTypes.EX)
        is JiuEngine -> context(
            engine,
            KartStateTypes.JIU,
            ::LiveJiuKartState,
        )
        is NewEngine -> nitroDraft(engine, KartStateTypes.NEW)
        is Z7Engine -> nitroDraft(engine, KartStateTypes.Z7)
        is V1Engine -> context(
            engine,
            KartStateTypes.V1,
            ::LiveV1KartState,
        )
        is A2Engine -> nitroDraft(engine, KartStateTypes.A2)
        is LegacyEngine -> nitroDraft(engine, KartStateTypes.LEGACY)
        is ProEngine -> nitroDraft(engine, KartStateTypes.PRO)
        is RushPlusEngine -> nitroDraft(engine, KartStateTypes.RUSHPLUS)
        is ChargeEngine -> context(
            engine,
            KartStateTypes.CHARGE,
            ::LiveChargeKartState,
        )
        is SREngine -> nitroDraft(engine, KartStateTypes.SR)
        is N1Engine -> nitroDraft(engine, KartStateTypes.N1)
        is RXEngine -> nitroDraft(engine, KartStateTypes.RX)
        is KeyEngine -> {
            val kart = specific(engine)
            val state: NitroKartState = LiveNitroKartState(kart)
            context(kart, KartStateTypes.KEY, state)
        }
        is GearEngine -> draftSpeed(engine, KartStateTypes.GEAR)
        is F1Engine -> draftSpeed(engine, KartStateTypes.F1)
        is RallyEngine -> draftSpeed(engine, KartStateTypes.RALLY)
        is MKEngine -> {
            val kart = specific(engine)
            val state: DraftKartState = LiveDraftKartState(kart)
            context(kart, KartStateTypes.MK, state)
        }
        is BoatEngine -> {
            val kart = specific(engine)
            val state: KartState = LiveKartState(kart)
            context(kart, KartStateTypes.BOAT, state)
        }
    }

    private fun <E> nitroDraft(
        engine: E,
        stateType: KartStateType<NitroDraftKartState>,
    ): HudSceneContext<NitroDraftKartState>
        where E : NitroEngine, E : DraftEngine {
        val kart = specific(engine)
        val state: NitroDraftKartState = LiveNitroDraftKartState(kart)
        return context(
            kart,
            stateType,
            state,
        )
    }

    private fun <E> draftSpeed(
        engine: E,
        stateType: KartStateType<DraftSpeedKartState>,
    ): HudSceneContext<DraftSpeedKartState>
        where E : SpeedEngine, E : DraftEngine {
        val kart = specific(engine)
        val state: DraftSpeedKartState = LiveDraftSpeedKartState(kart)
        return context(
            kart,
            stateType,
            state,
        )
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
        @Suppress("UNCHECKED_CAST")
        val engineClass = engine.javaClass as Class<E>
        return KartRef.Specific(engine, engineClass)
    }
}
