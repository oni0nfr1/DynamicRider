package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.RankingState

/** Preview preset이 요구하는 가변 카트 상태 capability다. */
enum class PreviewPresetRequirement {
    ANY,
    SPEED,
    DRIFT,
    DRAFT,
    NITRO,
    GEARLIKE,
    INSTANT_BOOST,
    DUAL_BOOST,
    EXCEED,
    NITRO_DRAFT,
    V1,
    CHARGE;

    /** [state]가 이 요구 조건을 만족하는지 반환한다. */
    fun accepts(state: KartState): Boolean = when (this) {
        ANY -> state is PreviewKartState
        SPEED -> state is PreviewSpeedKartState
        DRIFT -> state is PreviewDriftKartState
        DRAFT -> state is PreviewDraftKartState
        NITRO -> state is PreviewNitroKartState
        GEARLIKE -> state is PreviewGearLikeKartState
        INSTANT_BOOST -> state is PreviewInstantBoostKartState
        DUAL_BOOST -> state is PreviewDualBoostKartState
        EXCEED -> state is PreviewExceedKartState
        NITRO_DRAFT -> state is PreviewNitroKartState && state is PreviewDraftKartState
        V1 -> state is PreviewV1KartState
        CHARGE -> state is PreviewChargeKartState
    }
}

/** Preview 카트에 적용할 capability별 원본 값이다. */
data class PreviewKartValues(
    val speed: Double? = null,
    val drift: PreviewDriftValues? = null,
    val nitro: PreviewNitroValues? = null,
    val gearlike: PreviewGearLikeValues? = null,
    val instantBoost: PreviewInstantBoostValues? = null,
    val dualBoost: PreviewDualBoostValues? = null,
    val draft: PreviewDraftValues? = null,
    val exceedGauge: Float? = null,
    val fusionActive: Boolean? = null,
    val chargerGauge: Float? = null,
    val ers: Int? = null,
    val turboGauge: Float? = null,
)

data class PreviewDriftValues(
    val isDrifting: Boolean = false,
    val accurateDriftState: Boolean = false,
)

data class PreviewNitroValues(
    val isBoosting: Boolean = false,
    val maxBoost: Int = 3,
    val nitro: Int = 2,
    val nitroGauge: Float = 0.65f,
    val teamNitro: Int = 0,
    val teamBoostGaugeAvailable: Boolean = false,
    val teamBoostGauge: Float = 0f,
)

data class PreviewGearLikeValues(
    val rpm: Double = 0.55,
    val gear: Int = 3,
)

data class PreviewInstantBoostValues(
    val ready: Boolean = false,
    val enabled: Boolean = true,
)

data class PreviewDualBoostValues(
    val active: Boolean = false,
    val charging: Boolean = false,
)

data class PreviewDraftValues(
    val active: Boolean = false,
    val charging: Boolean = false,
)

/** Preview 경기에 적용할 진행 값이다. */
data class PreviewRaceValues(
    val racing: Boolean = true,
    val elapsedTimeMillis: Long = 42_000L,
    val currentLap: Int = 2,
    val maxLap: Int? = 3,
    val bestLapTimeMillis: Long? = 58_000L,
)

/** 편집기에서 대표 HUD 상태를 재현하기 위한 불변 값 묶음이다. */
data class PreviewStatePreset(
    val id: String,
    val displayNameKey: String = "dynamicrider.hud.editor.preview_preset.$id",
    val requirement: PreviewPresetRequirement = PreviewPresetRequirement.ANY,
    val kart: PreviewKartValues = PreviewKartValues(),
    val race: PreviewRaceValues = PreviewRaceValues(),
    val ranking: RankingState = RankingState.Unavailable(),
) {
    /** [state]에 이 preset을 적용할 수 있는지 반환한다. */
    fun isCompatibleWith(state: KartState): Boolean = requirement.accepts(state)
}
