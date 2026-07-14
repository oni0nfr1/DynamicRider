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

interface JiuKartState : NitroDraftKartState

interface ChargeKartState : NitroDraftKartState {
    val chargerGauge: Float
}

interface V1KartState : NitroDraftKartState {
    val exceedGauge: Float
}
