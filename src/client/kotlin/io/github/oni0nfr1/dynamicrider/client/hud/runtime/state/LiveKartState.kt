package io.github.oni0nfr1.dynamicrider.client.hud.runtime.state

import io.github.oni0nfr1.dynamicrider.client.hud.state.*
import io.github.oni0nfr1.dynamicrider.client.hud.state.internal.*
import io.github.oni0nfr1.dynamicrider.client.rider.backend.bossbar.KartTeamBoostTracker
import io.github.oni0nfr1.dynamicrider.client.rider.backend.inventory.KartTeamBoostCounter
import io.github.oni0nfr1.skid.client.api.engine.*
import io.github.oni0nfr1.skid.client.api.kart.Kart
import io.github.oni0nfr1.skid.client.api.utils.Ref
import io.github.oni0nfr1.skid.client.api.utils.access

internal class LiveDriftKartState<E : DriftEngine>(private val kart: Ref<Kart<E>>) : DriftKartStateFields {
    override val isDrifting: Boolean
        get() = kart.access { engine.isDrifting } ?: false
    override val accurateDriftState: Boolean
        get() = kart.access { engine.accurateDriftState } ?: false
}

internal class LiveSpeedKartState<E : SpeedEngine>(private val kart: Ref<Kart<E>>) : SpeedKartStateFields {
    override val speed: Double
        get() = kart.access { engine.tachometer?.speed } ?: 0.0
}

internal class LiveNitroKartState<E : NitroEngine>(private val kart: Ref<Kart<E>>) : NitroKartStateFields {
    override val isBoosting: Boolean
        get() = kart.access { engine.isBoosting } ?: false
    override val maxBoost: Int
        get() = kart.access { engine.maxBoost } ?: 2
    override val nitro: Int
        get() = kart.access { engine.tachometer?.nitro } ?: 0
    override val nitroGauge: Float
        get() = kart.access { engine.tachometer?.gauge?.toFloat() } ?: 0f
    override val teamNitro: Int
        get() = KartTeamBoostCounter.boostCount
    override val teamBoostGaugeAvailable: Boolean
        get() = KartTeamBoostTracker.gaugeExists
    override val teamBoostGauge: Float
        get() = if (teamBoostGaugeAvailable) KartTeamBoostTracker.gauge else 0f
}

internal class LiveGearLikeKartState<E : GearLikeEngine>(private val kart: Ref<Kart<E>>) : GearLikeKartStateFields {
    override val rpm: Double
        get() = kart.access { engine.tachometer?.rpm } ?: 0.0
    override val gear: Int
        get() = kart.access { engine.tachometer?.gear } ?: 0
}

internal class LiveInstantBoostKartState<E : InstantBoostEngine>(private val kart: Ref<Kart<E>>) : InstantBoostKartStateFields {
    override val instantBoostReady: Boolean
        get() = kart.access { engine.instantBoostReady } ?: false
    override val instantBoostEnabled: Boolean
        get() = kart.access { engine.instantBoostEnabled } ?: false
}

internal class LiveDualBoostKartState<E : DualBoostEngine>(private val kart: Ref<Kart<E>>) : DualBoostKartStateFields {
    override val dualBoostActive: Boolean
        get() = kart.access { engine.dualBoostActive } ?: false
    override val dualBoostCharging: Boolean
        get() = kart.access { engine.dualBoostCharging } ?: false
}

internal class LiveDraftKartState<E : DraftEngine>(private val kart: Ref<Kart<E>>) : DraftKartStateFields {
    override val draftActive: Boolean
        get() = kart.access { engine.draftActive } ?: false
    override val draftCharging: Boolean
        get() = kart.access { engine.draftCharging } ?: false
}

internal class LiveExceedKartState<E : ExceedEngine>(private val kart: Ref<Kart<E>>) : ExceedKartStateFields {
    override val exceedGauge: Float
        get() = kart.access { engine.tachometer?.exceedGauge } ?: 0f
}

internal class LiveRushPlusKartStateFields(
    private val kart: Ref<Kart<RushPlusEngine>>,
) : RushPlusKartStateFields {
    override val fusionActive: Boolean
        get() = kart.access { engine.tachometer?.fusionActive } ?: false
}

internal class LiveChargeKartStateFields(
    private val kart: Ref<Kart<ChargeEngine>>,
) : ChargeKartStateFields {
    override val chargerGauge: Float
        get() = kart.access { engine.tachometer?.chargerGauge } ?: 0f
}

internal class LiveF1KartStateFields(
    private val kart: Ref<Kart<F1Engine>>,
) : F1KartStateFields {
    override val ers: Int
        get() = kart.access { engine.tachometer?.ers } ?: 0
}

internal class LiveMKLikeKartState<E : MKLikeEngine>(
    private val kart: Ref<Kart<E>>,
) : MKLikeKartStateFields {
    override val turboGauge: Float
        get() = kart.access { engine.tachometer?.turboGauge?.toFloat() } ?: 0f
}

internal class LiveXKartState(kart: Ref<Kart<XEngine>>) : XKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DualBoostKartStateFields by LiveDualBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveEXKartState(kart: Ref<Kart<EXEngine>>) : EXKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DualBoostKartStateFields by LiveDualBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveJiuKartState(kart: Ref<Kart<JiuEngine>>) : JiuKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveNewKartState(kart: Ref<Kart<NewEngine>>) : NewKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveZ7KartState(kart: Ref<Kart<Z7Engine>>) : Z7KartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveV1KartState(kart: Ref<Kart<V1Engine>>) : V1KartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DualBoostKartStateFields by LiveDualBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart),
    ExceedKartStateFields by LiveExceedKartState(kart)

internal class LiveA2KartState(kart: Ref<Kart<A2Engine>>) : A2KartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveLegacyKartState(kart: Ref<Kart<LegacyEngine>>) : LegacyKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DualBoostKartStateFields by LiveDualBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveProKartState(kart: Ref<Kart<ProEngine>>) : ProKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveRushPlusKartState(kart: Ref<Kart<RushPlusEngine>>) : RushPlusKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart),
    ExceedKartStateFields by LiveExceedKartState(kart),
    RushPlusKartStateFields by LiveRushPlusKartStateFields(kart)

internal class LiveChargeKartState(kart: Ref<Kart<ChargeEngine>>) : ChargeKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart),
    ChargeKartStateFields by LiveChargeKartStateFields(kart)

internal class LiveSRKartState(kart: Ref<Kart<SREngine>>) : SRKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveN1KartState(kart: Ref<Kart<N1Engine>>) : N1KartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveRXKartState(kart: Ref<Kart<RXEngine>>) : RXKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart),
    InstantBoostKartStateFields by LiveInstantBoostKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveKeyKartState(kart: Ref<Kart<KeyEngine>>) : KeyKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    NitroKartStateFields by LiveNitroKartState(kart)

internal class LiveGearKartState(kart: Ref<Kart<GearEngine>>) : GearKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    GearLikeKartStateFields by LiveGearLikeKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveF1KartState(kart: Ref<Kart<F1Engine>>) : F1KartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    GearLikeKartStateFields by LiveGearLikeKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart),
    F1KartStateFields by LiveF1KartStateFields(kart)

internal class LiveRallyKartState(kart: Ref<Kart<RallyEngine>>) : RallyKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    SpeedKartStateFields by LiveSpeedKartState(kart),
    GearLikeKartStateFields by LiveGearLikeKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart)

internal class LiveMKKartState(kart: Ref<Kart<MKEngine>>) : MKKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart),
    MKLikeKartStateFields by LiveMKLikeKartState(kart)

internal class LiveDSKartState(kart: Ref<Kart<DSEngine>>) : DSKartState,
    DriftKartStateFields by LiveDriftKartState(kart),
    DraftKartStateFields by LiveDraftKartState(kart),
    MKLikeKartStateFields by LiveMKLikeKartState(kart)

internal class LiveBoatKartState : BoatKartState
