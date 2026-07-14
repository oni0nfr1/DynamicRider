package io.github.oni0nfr1.dynamicrider.client.hud.runtime.state

import io.github.oni0nfr1.dynamicrider.client.hud.state.*
import io.github.oni0nfr1.dynamicrider.client.rider.backend.bossbar.KartTeamBoostTracker
import io.github.oni0nfr1.dynamicrider.client.rider.backend.inventory.KartTeamBoostCounter
import io.github.oni0nfr1.skid.client.api.engine.*
import io.github.oni0nfr1.skid.client.api.kart.KartRef

class LiveSpeedKartState<E : SpeedEngine>(
    private val kart: KartRef.Specific<E>,
) : SpeedKartState {
    override val speed: Double
        get() = kart.accessEngine { it.tachometer?.speed } ?: 0.0
}

class LiveNitroKartState<E : NitroEngine>(
    private val kart: KartRef.Specific<E>,
) : NitroKartState,
    SpeedKartState by LiveSpeedKartState(kart) {
    override val isDrifting: Boolean
        get() = kart.accessEngine { it.isDrifting } ?: false
    override val isBoosting: Boolean
        get() = kart.accessEngine { it.isBoosting } ?: false
    override val maxBoost: Int
        get() = kart.accessEngine { it.maxBoost } ?: 2
    override val nitro: Int
        get() = kart.accessEngine { it.tachometer?.nitro } ?: 0
    override val nitroGauge: Float
        get() = kart.accessEngine { it.tachometer?.gauge?.toFloat() } ?: 0f
    override val teamNitro: Int
        get() = KartTeamBoostCounter.boostCount
    override val teamBoostGaugeAvailable: Boolean
        get() = KartTeamBoostTracker.gaugeExists
    override val teamBoostGauge: Float
        get() = if (teamBoostGaugeAvailable) KartTeamBoostTracker.gauge else 0f
}

class LiveDraftKartState<E>(
    private val kart: KartRef.Specific<E>,
) : DraftKartState
    where E : KartEngine, E : DraftEngine {
    override val draftActive: Boolean
        get() = kart.accessEngine { it.draftActive } ?: false
    override val draftCharging: Boolean
        get() = kart.accessEngine { it.draftCharging } ?: false
}

class LiveXKartState(
    kart: KartRef.Specific<XEngine>,
) : XKartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveEXKartState(
    kart: KartRef.Specific<EXEngine>,
) : EXKartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveJiuKartState(
    kart: KartRef.Specific<JiuEngine>,
) : JiuKartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveNewKartState(
    kart: KartRef.Specific<NewEngine>,
) : NewKartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveZ7KartState(
    kart: KartRef.Specific<Z7Engine>,
) : Z7KartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveV1KartState(
    private val kart: KartRef.Specific<V1Engine>,
) : V1KartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart) {
    override val exceedGauge: Float
        get() = kart.accessEngine { it.tachometer?.exceedGauge?.div(0.9851485f) } ?: 0f
}

class LiveA2KartState(
    kart: KartRef.Specific<A2Engine>,
) : A2KartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveLegacyKartState(
    kart: KartRef.Specific<LegacyEngine>,
) : LegacyKartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveProKartState(
    kart: KartRef.Specific<ProEngine>,
) : ProKartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveRushPlusKartState(
    kart: KartRef.Specific<RushPlusEngine>,
) : RushPlusKartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveChargeKartState(
    private val kart: KartRef.Specific<ChargeEngine>,
) : ChargeKartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart) {
    override val chargerGauge: Float
        get() = kart.accessEngine { it.tachometer?.chargerGauge } ?: 0f
}

class LiveSRKartState(
    kart: KartRef.Specific<SREngine>,
) : SRKartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveN1KartState(
    kart: KartRef.Specific<N1Engine>,
) : N1KartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveRXKartState(
    kart: KartRef.Specific<RXEngine>,
) : RXKartState,
    NitroKartState by LiveNitroKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveKeyKartState(
    kart: KartRef.Specific<KeyEngine>,
) : KeyKartState,
    NitroKartState by LiveNitroKartState(kart)

class LiveGearKartState(
    kart: KartRef.Specific<GearEngine>,
) : GearKartState,
    SpeedKartState by LiveSpeedKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveF1KartState(
    kart: KartRef.Specific<F1Engine>,
) : F1KartState,
    SpeedKartState by LiveSpeedKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveRallyKartState(
    kart: KartRef.Specific<RallyEngine>,
) : RallyKartState,
    SpeedKartState by LiveSpeedKartState(kart),
    DraftKartState by LiveDraftKartState(kart)

class LiveMKKartState(
    kart: KartRef.Specific<MKEngine>,
) : MKKartState,
    DraftKartState by LiveDraftKartState(kart)

class LiveBoatKartState : BoatKartState
