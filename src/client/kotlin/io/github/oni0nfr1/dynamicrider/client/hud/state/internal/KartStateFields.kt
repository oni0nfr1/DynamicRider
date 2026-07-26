package io.github.oni0nfr1.dynamicrider.client.hud.state.internal

/** 공개 capability 계층과 독립적으로 live/preview 구현을 조합하는 원자 프로퍼티 계약이다. */
internal interface DriftKartStateFields {
    val isDrifting: Boolean
    val accurateDriftState: Boolean
}

internal interface SpeedKartStateFields {
    val speed: Double
}

internal interface NitroKartStateFields {
    val isBoosting: Boolean
    val maxBoost: Int
    val nitro: Int
    val nitroGauge: Float
    val teamNitro: Int
    val teamBoostGaugeAvailable: Boolean
    val teamBoostGauge: Float
}

internal interface GearLikeKartStateFields {
    val rpm: Double
    val gear: Int
}

internal interface InstantBoostKartStateFields {
    val instantBoostReady: Boolean
    val instantBoostEnabled: Boolean
}

internal interface DualBoostKartStateFields {
    val dualBoostActive: Boolean
    val dualBoostCharging: Boolean
}

internal interface DraftKartStateFields {
    val draftActive: Boolean
    val draftCharging: Boolean
}

internal interface ExceedKartStateFields {
    val exceedGauge: Float
}

internal interface RushPlusKartStateFields {
    val fusionActive: Boolean
}

internal interface ChargeKartStateFields {
    val chargerGauge: Float
}

internal interface F1KartStateFields {
    val ers: Int
}

internal interface MKLikeKartStateFields {
    val turboGauge: Float
}
