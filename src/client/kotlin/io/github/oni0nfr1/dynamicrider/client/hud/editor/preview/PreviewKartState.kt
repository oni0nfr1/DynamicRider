package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.state.*

/** 편집기가 값을 변경할 수 있는 카트 상태의 최상위 계약이다. */
interface PreviewKartState : KartState

interface PreviewSpeedKartState : PreviewKartState, SpeedKartState {
    override var speed: Double
}

interface PreviewDraftKartState : PreviewKartState, DraftKartState {
    override var draftActive: Boolean
    override var draftCharging: Boolean
}

interface PreviewNitroKartState : PreviewSpeedKartState, NitroKartState {
    override var isDrifting: Boolean
    override var isBoosting: Boolean
    override var maxBoost: Int
    override var nitro: Int
    override var nitroGauge: Float
    override var teamNitro: Int
    override var teamBoostGaugeAvailable: Boolean
    override var teamBoostGauge: Float
}

interface PreviewDraftSpeedKartState :
    PreviewSpeedKartState,
    PreviewDraftKartState,
    DraftSpeedKartState

interface PreviewNitroDraftKartState :
    PreviewNitroKartState,
    PreviewDraftKartState,
    NitroDraftKartState

interface PreviewXKartState : PreviewNitroDraftKartState, XKartState
interface PreviewEXKartState : PreviewNitroDraftKartState, EXKartState
interface PreviewJiuKartState : PreviewNitroDraftKartState, JiuKartState
interface PreviewNewKartState : PreviewNitroDraftKartState, NewKartState
interface PreviewZ7KartState : PreviewNitroDraftKartState, Z7KartState

interface PreviewV1KartState : PreviewNitroDraftKartState, V1KartState {
    override var exceedGauge: Float
}

interface PreviewA2KartState : PreviewNitroDraftKartState, A2KartState
interface PreviewLegacyKartState : PreviewNitroDraftKartState, LegacyKartState
interface PreviewProKartState : PreviewNitroDraftKartState, ProKartState
interface PreviewRushPlusKartState : PreviewNitroDraftKartState, RushPlusKartState

interface PreviewChargeKartState : PreviewNitroDraftKartState, ChargeKartState {
    override var chargerGauge: Float
}

interface PreviewSRKartState : PreviewNitroDraftKartState, SRKartState
interface PreviewN1KartState : PreviewNitroDraftKartState, N1KartState
interface PreviewRXKartState : PreviewNitroDraftKartState, RXKartState
interface PreviewKeyKartState : PreviewNitroKartState, KeyKartState
interface PreviewGearKartState : PreviewDraftSpeedKartState, GearKartState
interface PreviewF1KartState : PreviewDraftSpeedKartState, F1KartState
interface PreviewRallyKartState : PreviewDraftSpeedKartState, RallyKartState
interface PreviewMKKartState : PreviewDraftKartState, MKKartState
interface PreviewBoatKartState : PreviewKartState, BoatKartState

class DefaultPreviewSpeedKartState(
    override var speed: Double = 169.9,
) : PreviewSpeedKartState

class DefaultPreviewDraftKartState(
    override var draftActive: Boolean = false,
    override var draftCharging: Boolean = false,
) : PreviewDraftKartState

class DefaultPreviewNitroKartState(
    val speedState: PreviewSpeedKartState = DefaultPreviewSpeedKartState(),
    override var isDrifting: Boolean = false,
    override var isBoosting: Boolean = false,
    override var maxBoost: Int = 3,
    override var nitro: Int = 2,
    override var nitroGauge: Float = 0.65f,
    override var teamNitro: Int = 0,
    override var teamBoostGaugeAvailable: Boolean = false,
    override var teamBoostGauge: Float = 0f,
) : PreviewNitroKartState,
    PreviewSpeedKartState by speedState

class DefaultPreviewXKartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewXKartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewEXKartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewEXKartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewJiuKartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewJiuKartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewNewKartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewNewKartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewZ7KartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewZ7KartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewV1KartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
    override var exceedGauge: Float = 0.4f,
) : PreviewV1KartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewA2KartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewA2KartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewLegacyKartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewLegacyKartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewProKartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewProKartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewRushPlusKartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewRushPlusKartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewChargeKartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
    override var chargerGauge: Float = 0.5f,
) : PreviewChargeKartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewSRKartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewSRKartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewN1KartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewN1KartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewRXKartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewRXKartState,
    PreviewNitroKartState by nitroState,
    PreviewDraftKartState by draftState

class DefaultPreviewKeyKartState(
    nitroState: PreviewNitroKartState = DefaultPreviewNitroKartState(),
) : PreviewKeyKartState,
    PreviewNitroKartState by nitroState

class DefaultPreviewGearKartState(
    speedState: PreviewSpeedKartState = DefaultPreviewSpeedKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewGearKartState,
    PreviewSpeedKartState by speedState,
    PreviewDraftKartState by draftState

class DefaultPreviewF1KartState(
    speedState: PreviewSpeedKartState = DefaultPreviewSpeedKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewF1KartState,
    PreviewSpeedKartState by speedState,
    PreviewDraftKartState by draftState

class DefaultPreviewRallyKartState(
    speedState: PreviewSpeedKartState = DefaultPreviewSpeedKartState(),
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewRallyKartState,
    PreviewSpeedKartState by speedState,
    PreviewDraftKartState by draftState

class DefaultPreviewMKKartState(
    draftState: PreviewDraftKartState = DefaultPreviewDraftKartState(),
) : PreviewMKKartState,
    PreviewDraftKartState by draftState

class DefaultPreviewBoatKartState : PreviewBoatKartState
