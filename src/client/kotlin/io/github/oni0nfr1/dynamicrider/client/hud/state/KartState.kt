package io.github.oni0nfr1.dynamicrider.client.hud.state

/** HUD가 관측할 수 있는 카트 상태의 최상위 계약이다. */
interface KartState

interface DriftKartState : KartState {
    val isDrifting: Boolean
    val accurateDriftState: Boolean
}

interface SpeedKartState : KartState {
    val speed: Double
}

interface DraftKartState : KartState {
    val draftActive: Boolean
    val draftCharging: Boolean
}

interface NitroKartState : DriftKartState, SpeedKartState {
    val isBoosting: Boolean
    val maxBoost: Int
    val nitro: Int
    val nitroGauge: Float
    val teamNitro: Int
    val teamBoostGaugeAvailable: Boolean
    val teamBoostGauge: Float
}

interface GearLikeKartState : DriftKartState, SpeedKartState {
    val rpm: Double
    val gear: Int
}

interface InstantBoostKartState : NitroKartState {
    val instantBoostReady: Boolean
    val instantBoostEnabled: Boolean
}

interface DualBoostKartState : NitroKartState {
    val dualBoostActive: Boolean
    val dualBoostCharging: Boolean
}

interface ExceedKartState : KartState {
    val exceedGauge: Float
}

interface XKartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DualBoostKartState, DraftKartState
interface EXKartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DualBoostKartState, DraftKartState
interface JiuKartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DraftKartState
interface NewKartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DraftKartState
interface Z7KartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DraftKartState
interface V1KartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DualBoostKartState, DraftKartState, ExceedKartState
interface A2KartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DraftKartState
interface LegacyKartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DualBoostKartState, DraftKartState
interface ProKartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DraftKartState

interface RushPlusKartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DraftKartState, ExceedKartState {
    val fusionActive: Boolean
}

interface ChargeKartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DraftKartState {
    val chargerGauge: Float
}

interface SRKartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DraftKartState
interface N1KartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DraftKartState
interface RXKartState : DriftKartState, SpeedKartState, NitroKartState, InstantBoostKartState, DraftKartState
interface KeyKartState : DriftKartState, SpeedKartState, NitroKartState
interface GearKartState : DriftKartState, SpeedKartState, GearLikeKartState, DraftKartState

interface F1KartState : DriftKartState, SpeedKartState, GearLikeKartState, DraftKartState {
    val ers: Int
}

interface RallyKartState : DriftKartState, SpeedKartState, GearLikeKartState, DraftKartState

interface MKLikeKartState : DriftKartState, DraftKartState {
    val turboGauge: Float
}

interface MKKartState : MKLikeKartState
interface DSKartState : MKLikeKartState

interface BoatKartState : KartState
