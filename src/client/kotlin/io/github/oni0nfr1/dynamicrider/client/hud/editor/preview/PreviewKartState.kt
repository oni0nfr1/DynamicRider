package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.state.*
import io.github.oni0nfr1.dynamicrider.client.hud.state.internal.*

/** 편집기가 값을 변경할 수 있는 카트 상태의 최상위 계약이다. */
interface PreviewKartState : KartState

interface PreviewDriftKartState : PreviewKartState, DriftKartState {
    override var isDrifting: Boolean
    override var accurateDriftState: Boolean
}

interface PreviewSpeedKartState : PreviewKartState, SpeedKartState {
    override var speed: Double
}

interface PreviewNitroKartState : PreviewDriftKartState, PreviewSpeedKartState, NitroKartState {
    override var isBoosting: Boolean
    override var maxBoost: Int
    override var nitro: Int
    override var nitroGauge: Float
    override var teamNitro: Int
    override var teamBoostGaugeAvailable: Boolean
    override var teamBoostGauge: Float
}

interface PreviewGearLikeKartState : PreviewDriftKartState, PreviewSpeedKartState, GearLikeKartState {
    override var rpm: Double
    override var gear: Int
}

interface PreviewInstantBoostKartState : PreviewNitroKartState, InstantBoostKartState {
    override var instantBoostReady: Boolean
    override var instantBoostEnabled: Boolean
}

interface PreviewDualBoostKartState : PreviewNitroKartState, DualBoostKartState {
    override var dualBoostActive: Boolean
    override var dualBoostCharging: Boolean
}

interface PreviewDraftKartState : PreviewKartState, DraftKartState {
    override var draftActive: Boolean
    override var draftCharging: Boolean
}

interface PreviewExceedKartState : PreviewKartState, ExceedKartState {
    override var exceedGauge: Float
}

interface PreviewXKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDualBoostKartState, PreviewDraftKartState, XKartState
interface PreviewEXKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDualBoostKartState, PreviewDraftKartState, EXKartState
interface PreviewJiuKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDraftKartState, JiuKartState
interface PreviewNewKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDraftKartState, NewKartState
interface PreviewZ7KartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDraftKartState, Z7KartState
interface PreviewV1KartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDualBoostKartState, PreviewDraftKartState,
    PreviewExceedKartState, V1KartState
interface PreviewA2KartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDraftKartState, A2KartState
interface PreviewLegacyKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDualBoostKartState, PreviewDraftKartState, LegacyKartState
interface PreviewProKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDraftKartState, ProKartState

interface PreviewRushPlusKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDraftKartState, PreviewExceedKartState, RushPlusKartState {
    override var fusionActive: Boolean
}

interface PreviewChargeKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDraftKartState, ChargeKartState {
    override var chargerGauge: Float
}

interface PreviewSRKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDraftKartState, SRKartState
interface PreviewN1KartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDraftKartState, N1KartState
interface PreviewRXKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState,
    PreviewInstantBoostKartState, PreviewDraftKartState, RXKartState
interface PreviewKeyKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewNitroKartState, KeyKartState
interface PreviewGearKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewGearLikeKartState,
    PreviewDraftKartState, GearKartState

interface PreviewF1KartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewGearLikeKartState,
    PreviewDraftKartState, F1KartState {
    override var ers: Int
}

interface PreviewRallyKartState : PreviewDriftKartState, PreviewSpeedKartState, PreviewGearLikeKartState,
    PreviewDraftKartState, RallyKartState

interface PreviewMKLikeKartState : PreviewDriftKartState, PreviewDraftKartState, MKLikeKartState {
    override var turboGauge: Float
}

interface PreviewMKKartState : PreviewMKLikeKartState, MKKartState
interface PreviewDSKartState : PreviewMKLikeKartState, DSKartState

interface PreviewBoatKartState : PreviewKartState, BoatKartState

internal interface MutableDriftKartStateFields : DriftKartStateFields {
    override var isDrifting: Boolean
    override var accurateDriftState: Boolean
}

internal interface MutableSpeedKartStateFields : SpeedKartStateFields {
    override var speed: Double
}

internal interface MutableNitroKartStateFields : NitroKartStateFields {
    override var isBoosting: Boolean
    override var maxBoost: Int
    override var nitro: Int
    override var nitroGauge: Float
    override var teamNitro: Int
    override var teamBoostGaugeAvailable: Boolean
    override var teamBoostGauge: Float
}

internal interface MutableGearLikeKartStateFields : GearLikeKartStateFields {
    override var rpm: Double
    override var gear: Int
}

internal interface MutableInstantBoostKartStateFields : InstantBoostKartStateFields {
    override var instantBoostReady: Boolean
    override var instantBoostEnabled: Boolean
}

internal interface MutableDualBoostKartStateFields : DualBoostKartStateFields {
    override var dualBoostActive: Boolean
    override var dualBoostCharging: Boolean
}

internal interface MutableDraftKartStateFields : DraftKartStateFields {
    override var draftActive: Boolean
    override var draftCharging: Boolean
}

internal interface MutableExceedKartStateFields : ExceedKartStateFields {
    override var exceedGauge: Float
}

internal interface MutableRushPlusKartStateFields : RushPlusKartStateFields {
    override var fusionActive: Boolean
}

internal interface MutableChargeKartStateFields : ChargeKartStateFields {
    override var chargerGauge: Float
}

internal interface MutableF1KartStateFields : F1KartStateFields {
    override var ers: Int
}

internal interface MutableMKLikeKartStateFields : MKLikeKartStateFields {
    override var turboGauge: Float
}

internal class DefaultPreviewDriftKartState(
    override var isDrifting: Boolean = false,
    override var accurateDriftState: Boolean = false,
) : MutableDriftKartStateFields

internal class DefaultPreviewSpeedKartState(
    override var speed: Double = 169.9,
) : MutableSpeedKartStateFields

internal class DefaultPreviewNitroKartState(
    override var isBoosting: Boolean = false,
    override var maxBoost: Int = 3,
    override var nitro: Int = 2,
    override var nitroGauge: Float = 0.65f,
    override var teamNitro: Int = 0,
    override var teamBoostGaugeAvailable: Boolean = false,
    override var teamBoostGauge: Float = 0f,
) : MutableNitroKartStateFields

internal class DefaultPreviewGearLikeKartState(
    override var rpm: Double = 0.55,
    override var gear: Int = 3,
) : MutableGearLikeKartStateFields

internal class DefaultPreviewInstantBoostKartState(
    override var instantBoostReady: Boolean = false,
    override var instantBoostEnabled: Boolean = true,
) : MutableInstantBoostKartStateFields

internal class DefaultPreviewDualBoostKartState(
    override var dualBoostActive: Boolean = false,
    override var dualBoostCharging: Boolean = false,
) : MutableDualBoostKartStateFields

internal class DefaultPreviewDraftKartState(
    override var draftActive: Boolean = false,
    override var draftCharging: Boolean = false,
) : MutableDraftKartStateFields

internal class DefaultPreviewExceedKartState(
    override var exceedGauge: Float = 0.4f,
) : MutableExceedKartStateFields

internal class DefaultPreviewRushPlusKartStateFields(
    override var fusionActive: Boolean = false,
) : MutableRushPlusKartStateFields

internal class DefaultPreviewChargeKartStateFields(
    override var chargerGauge: Float = 0.5f,
) : MutableChargeKartStateFields

internal class DefaultPreviewF1KartStateFields(
    override var ers: Int = 50,
) : MutableF1KartStateFields

internal class DefaultPreviewMKLikeKartStateFields(
    override var turboGauge: Float = 0.3f,
) : MutableMKLikeKartStateFields

internal class DefaultPreviewXKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    dual: MutableDualBoostKartStateFields = DefaultPreviewDualBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewXKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDualBoostKartStateFields by dual, MutableDraftKartStateFields by draft

internal class DefaultPreviewEXKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    dual: MutableDualBoostKartStateFields = DefaultPreviewDualBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewEXKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDualBoostKartStateFields by dual, MutableDraftKartStateFields by draft

internal class DefaultPreviewJiuKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewJiuKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDraftKartStateFields by draft

internal class DefaultPreviewNewKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewNewKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDraftKartStateFields by draft

internal class DefaultPreviewZ7KartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewZ7KartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDraftKartStateFields by draft

internal class DefaultPreviewV1KartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    dual: MutableDualBoostKartStateFields = DefaultPreviewDualBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
    exceed: MutableExceedKartStateFields = DefaultPreviewExceedKartState(),
) : PreviewV1KartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDualBoostKartStateFields by dual, MutableDraftKartStateFields by draft,
    MutableExceedKartStateFields by exceed

internal class DefaultPreviewA2KartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewA2KartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDraftKartStateFields by draft

internal class DefaultPreviewLegacyKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    dual: MutableDualBoostKartStateFields = DefaultPreviewDualBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewLegacyKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDualBoostKartStateFields by dual, MutableDraftKartStateFields by draft

internal class DefaultPreviewProKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewProKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDraftKartStateFields by draft

internal class DefaultPreviewRushPlusKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
    exceed: MutableExceedKartStateFields = DefaultPreviewExceedKartState(),
    rushPlus: MutableRushPlusKartStateFields = DefaultPreviewRushPlusKartStateFields(),
) : PreviewRushPlusKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDraftKartStateFields by draft, MutableExceedKartStateFields by exceed,
    MutableRushPlusKartStateFields by rushPlus

internal class DefaultPreviewChargeKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
    charge: MutableChargeKartStateFields = DefaultPreviewChargeKartStateFields(),
) : PreviewChargeKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDraftKartStateFields by draft, MutableChargeKartStateFields by charge

internal class DefaultPreviewSRKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewSRKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDraftKartStateFields by draft

internal class DefaultPreviewN1KartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewN1KartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDraftKartStateFields by draft

internal class DefaultPreviewRXKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
    instant: MutableInstantBoostKartStateFields = DefaultPreviewInstantBoostKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewRXKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro, MutableInstantBoostKartStateFields by instant,
    MutableDraftKartStateFields by draft

internal class DefaultPreviewKeyKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    nitro: MutableNitroKartStateFields = DefaultPreviewNitroKartState(),
) : PreviewKeyKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableNitroKartStateFields by nitro

internal class DefaultPreviewGearKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    gearlike: MutableGearLikeKartStateFields = DefaultPreviewGearLikeKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewGearKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableGearLikeKartStateFields by gearlike, MutableDraftKartStateFields by draft

internal class DefaultPreviewF1KartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    gearlike: MutableGearLikeKartStateFields = DefaultPreviewGearLikeKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
    f1: MutableF1KartStateFields = DefaultPreviewF1KartStateFields(),
) : PreviewF1KartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableGearLikeKartStateFields by gearlike, MutableDraftKartStateFields by draft,
    MutableF1KartStateFields by f1

internal class DefaultPreviewRallyKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    speed: MutableSpeedKartStateFields = DefaultPreviewSpeedKartState(),
    gearlike: MutableGearLikeKartStateFields = DefaultPreviewGearLikeKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
) : PreviewRallyKartState, MutableDriftKartStateFields by drift, MutableSpeedKartStateFields by speed,
    MutableGearLikeKartStateFields by gearlike, MutableDraftKartStateFields by draft

internal class DefaultPreviewMKKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
    mkLike: MutableMKLikeKartStateFields = DefaultPreviewMKLikeKartStateFields(),
) : PreviewMKKartState, MutableDriftKartStateFields by drift, MutableDraftKartStateFields by draft,
    MutableMKLikeKartStateFields by mkLike

internal class DefaultPreviewDSKartState(
    drift: MutableDriftKartStateFields = DefaultPreviewDriftKartState(),
    draft: MutableDraftKartStateFields = DefaultPreviewDraftKartState(),
    mkLike: MutableMKLikeKartStateFields = DefaultPreviewMKLikeKartStateFields(),
) : PreviewDSKartState, MutableDriftKartStateFields by drift, MutableDraftKartStateFields by draft,
    MutableMKLikeKartStateFields by mkLike

internal class DefaultPreviewBoatKartState : PreviewBoatKartState
