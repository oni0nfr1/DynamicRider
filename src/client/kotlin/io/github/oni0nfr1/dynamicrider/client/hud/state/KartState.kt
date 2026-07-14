package io.github.oni0nfr1.dynamicrider.client.hud.state

/** HUD가 관측할 수 있는 카트 상태의 최상위 계약이다. */
interface KartState

interface SpeedKartState : KartState {
    val speed: Double
}

interface DraftKartState : KartState {
    val draftActive: Boolean
    val draftCharging: Boolean
}

interface NitroKartState : SpeedKartState {
    val isDrifting: Boolean
    val isBoosting: Boolean
    val maxBoost: Int
    val nitro: Int
    val nitroGauge: Float
    val teamNitro: Int
    val teamBoostGaugeAvailable: Boolean
    val teamBoostGauge: Float
}

interface DraftSpeedKartState : SpeedKartState, DraftKartState

interface NitroDraftKartState : NitroKartState, DraftKartState

interface XKartState : NitroDraftKartState
interface EXKartState : NitroDraftKartState
interface JiuKartState : NitroDraftKartState
interface NewKartState : NitroDraftKartState
interface Z7KartState : NitroDraftKartState

interface ChargeKartState : NitroDraftKartState {
    val chargerGauge: Float
}

interface V1KartState : NitroDraftKartState {
    val exceedGauge: Float
}

interface A2KartState : NitroDraftKartState
interface LegacyKartState : NitroDraftKartState
interface ProKartState : NitroDraftKartState
interface RushPlusKartState : NitroDraftKartState
interface SRKartState : NitroDraftKartState
interface N1KartState : NitroDraftKartState
interface RXKartState : NitroDraftKartState
interface KeyKartState : NitroKartState
interface GearKartState : DraftSpeedKartState
interface F1KartState : DraftSpeedKartState
interface RallyKartState : DraftSpeedKartState
interface MKKartState : DraftKartState
interface BoatKartState : KartState
