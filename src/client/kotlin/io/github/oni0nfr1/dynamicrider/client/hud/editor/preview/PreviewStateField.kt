package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudNumberType
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudNumericRange
import kotlinx.serialization.json.*

/** Preview 상태 탭에 노출할 하나의 가변 상태 값이다. */
data class PreviewStateField(
    val id: String,
    val nameKey: String,
    val value: JsonElement,
    val editor: Editor,
) {
    sealed interface Editor {
        data object Toggle : Editor

        data class Number(
            val numberType: HudNumberType,
            val range: HudNumericRange? = null,
            val nullable: Boolean = false,
        ) : Editor
    }
}

sealed interface PreviewStateUpdateResult {
    data object Updated : PreviewStateUpdateResult
    data object Unchanged : PreviewStateUpdateResult
    data class FieldNotFound(val fieldId: String) : PreviewStateUpdateResult
    data class InvalidValue(val fieldId: String) : PreviewStateUpdateResult
}

/** Preview context의 capability를 편집 가능한 필드 snapshot과 안전한 갱신 작업으로 변환한다. */
object PreviewStateFieldEditor {
    fun fields(context: PreviewHudSceneContext<*>): List<PreviewStateField> = buildList {
        val state = context.kartState
        if (state is PreviewSpeedKartState) {
            add(number(SPEED, state.speed, HudNumberType.DOUBLE, 0.0, 400.0, 0.1))
        }
        if (state is PreviewDriftKartState) {
            add(toggle(DRIFTING, state.isDrifting))
            add(toggle(ACCURATE_DRIFT_STATE, state.accurateDriftState))
        }
        if (state is PreviewNitroKartState) {
            add(toggle(BOOSTING, state.isBoosting))
            add(number(MAX_BOOST, state.maxBoost, HudNumberType.INT, 0.0, 10.0, 1.0))
            add(number(NITRO, state.nitro, HudNumberType.INT, 0.0, 10.0, 1.0))
            add(number(NITRO_GAUGE, state.nitroGauge, HudNumberType.FLOAT, 0.0, 1.0, 0.01))
            add(number(TEAM_NITRO, state.teamNitro, HudNumberType.INT, 0.0, 10.0, 1.0))
            add(toggle(TEAM_GAUGE_AVAILABLE, state.teamBoostGaugeAvailable))
            add(number(TEAM_GAUGE, state.teamBoostGauge, HudNumberType.FLOAT, 0.0, 1.0, 0.01))
        }
        if (state is PreviewGearLikeKartState) {
            add(number(RPM, state.rpm, HudNumberType.DOUBLE, 0.0, 1.0, 0.01))
            add(number(GEAR, state.gear, HudNumberType.INT, 0.0, 10.0, 1.0))
        }
        if (state is PreviewInstantBoostKartState) {
            add(toggle(INSTANT_BOOST_READY, state.instantBoostReady))
            add(toggle(INSTANT_BOOST_ENABLED, state.instantBoostEnabled))
        }
        if (state is PreviewDualBoostKartState) {
            add(toggle(DUAL_BOOST_ACTIVE, state.dualBoostActive))
            add(toggle(DUAL_BOOST_CHARGING, state.dualBoostCharging))
        }
        if (state is PreviewDraftKartState) {
            add(toggle(DRAFT_ACTIVE, state.draftActive))
            add(toggle(DRAFT_CHARGING, state.draftCharging))
        }
        if (state is PreviewExceedKartState) {
            add(number(EXCEED_GAUGE, state.exceedGauge, HudNumberType.FLOAT, 0.0, 1.0, 0.01))
        }
        if (state is PreviewRushPlusKartState) {
            add(toggle(FUSION_ACTIVE, state.fusionActive))
        }
        if (state is PreviewChargeKartState) {
            add(number(CHARGER_GAUGE, state.chargerGauge, HudNumberType.FLOAT, 0.0, 1.0, 0.01))
        }
        if (state is PreviewF1KartState) {
            add(number(ERS, state.ers, HudNumberType.INT, 0.0, 100.0, 1.0))
        }
        if (state is PreviewMKLikeKartState) {
            add(number(TURBO_GAUGE, state.turboGauge, HudNumberType.FLOAT, 0.0, 1.0, 0.01))
        }
        val race = context.raceState
        add(toggle(RACING, race.racing))
        add(number(ELAPSED_TIME, race.elapsedTimeMillis, HudNumberType.LONG, 0.0, 3_600_000.0, null))
        add(number(CURRENT_LAP, race.currentLap, HudNumberType.INT, 0.0, 99.0, 1.0))
        add(nullableNumber(MAX_LAP, race.maxLap, HudNumberType.INT, 0.0, 99.0))
        add(nullableNumber(BEST_LAP_TIME, race.bestLapTimeMillis, HudNumberType.LONG, 0.0, 3_600_000.0))
    }

    fun update(
        context: PreviewHudSceneContext<*>,
        fieldId: String,
        value: JsonElement,
    ): PreviewStateUpdateResult {
        val field = fields(context).firstOrNull { it.id == fieldId }
            ?: return PreviewStateUpdateResult.FieldNotFound(fieldId)
        if (!isValid(field, value)) return PreviewStateUpdateResult.InvalidValue(fieldId)
        if (field.value == value) return PreviewStateUpdateResult.Unchanged
        val state = context.kartState
        val primitive = value as? JsonPrimitive
        when (fieldId) {
            SPEED -> (state as PreviewSpeedKartState).speed = primitive!!.double
            DRIFTING -> (state as PreviewDriftKartState).isDrifting = primitive!!.boolean
            ACCURATE_DRIFT_STATE -> (state as PreviewDriftKartState).accurateDriftState = primitive!!.boolean
            BOOSTING -> (state as PreviewNitroKartState).isBoosting = primitive!!.boolean
            MAX_BOOST -> (state as PreviewNitroKartState).maxBoost = primitive!!.int
            NITRO -> (state as PreviewNitroKartState).nitro = primitive!!.int
            NITRO_GAUGE -> (state as PreviewNitroKartState).nitroGauge = primitive!!.float
            TEAM_NITRO -> (state as PreviewNitroKartState).teamNitro = primitive!!.int
            TEAM_GAUGE_AVAILABLE -> (state as PreviewNitroKartState).teamBoostGaugeAvailable = primitive!!.boolean
            TEAM_GAUGE -> (state as PreviewNitroKartState).teamBoostGauge = primitive!!.float
            RPM -> (state as PreviewGearLikeKartState).rpm = primitive!!.double
            GEAR -> (state as PreviewGearLikeKartState).gear = primitive!!.int
            INSTANT_BOOST_READY -> (state as PreviewInstantBoostKartState).instantBoostReady = primitive!!.boolean
            INSTANT_BOOST_ENABLED -> (state as PreviewInstantBoostKartState).instantBoostEnabled = primitive!!.boolean
            DUAL_BOOST_ACTIVE -> (state as PreviewDualBoostKartState).dualBoostActive = primitive!!.boolean
            DUAL_BOOST_CHARGING -> (state as PreviewDualBoostKartState).dualBoostCharging = primitive!!.boolean
            DRAFT_ACTIVE -> (state as PreviewDraftKartState).draftActive = primitive!!.boolean
            DRAFT_CHARGING -> (state as PreviewDraftKartState).draftCharging = primitive!!.boolean
            EXCEED_GAUGE -> (state as PreviewExceedKartState).exceedGauge = primitive!!.float
            FUSION_ACTIVE -> (state as PreviewRushPlusKartState).fusionActive = primitive!!.boolean
            CHARGER_GAUGE -> (state as PreviewChargeKartState).chargerGauge = primitive!!.float
            ERS -> (state as PreviewF1KartState).ers = primitive!!.int
            TURBO_GAUGE -> (state as PreviewMKLikeKartState).turboGauge = primitive!!.float
            RACING -> context.raceState.racing = primitive!!.boolean
            ELAPSED_TIME -> context.raceState.elapsedTimeMillis = primitive!!.long
            CURRENT_LAP -> context.raceState.currentLap = primitive!!.int
            MAX_LAP -> context.raceState.maxLap = if (value === JsonNull) null else primitive!!.int
            BEST_LAP_TIME -> context.raceState.bestLapTimeMillis = if (value === JsonNull) null else primitive!!.long
        }
        return PreviewStateUpdateResult.Updated
    }

    private fun isValid(field: PreviewStateField, value: JsonElement): Boolean = when (val editor = field.editor) {
        PreviewStateField.Editor.Toggle -> (value as? JsonPrimitive)?.booleanOrNull != null
        is PreviewStateField.Editor.Number -> {
            if (value === JsonNull) return editor.nullable
            val primitive = value as? JsonPrimitive ?: return false
            val number = primitive.doubleOrNull ?: return false
            val correctType = when (editor.numberType) {
                HudNumberType.BYTE, HudNumberType.SHORT, HudNumberType.INT -> primitive.intOrNull != null
                HudNumberType.LONG -> primitive.longOrNull != null
                HudNumberType.FLOAT, HudNumberType.DOUBLE -> true
            }
            correctType && editor.range?.let { number in it.min..it.max } != false
        }
    }

    private fun toggle(id: String, value: Boolean) = PreviewStateField(
        id,
        key(id),
        JsonPrimitive(value),
        PreviewStateField.Editor.Toggle,
    )

    private fun number(
        id: String,
        value: Number,
        type: HudNumberType,
        min: Double,
        max: Double,
        step: Double?,
    ) = PreviewStateField(
        id,
        key(id),
        JsonPrimitive(value),
        PreviewStateField.Editor.Number(type, HudNumericRange(min, max, step)),
    )

    private fun nullableNumber(
        id: String,
        value: Number?,
        type: HudNumberType,
        min: Double,
        max: Double,
    ) = PreviewStateField(
        id,
        key(id),
        value?.let(::JsonPrimitive) ?: JsonNull,
        PreviewStateField.Editor.Number(type, HudNumericRange(min, max, null), nullable = true),
    )

    private fun key(id: String) = "dynamicrider.hud.editor.preview_state.$id"

    private const val SPEED = "speed"
    private const val DRIFTING = "drifting"
    private const val ACCURATE_DRIFT_STATE = "accurate_drift_state"
    private const val BOOSTING = "boosting"
    private const val MAX_BOOST = "max_boost"
    private const val NITRO = "nitro"
    private const val NITRO_GAUGE = "nitro_gauge"
    private const val TEAM_NITRO = "team_nitro"
    private const val TEAM_GAUGE_AVAILABLE = "team_gauge_available"
    private const val TEAM_GAUGE = "team_gauge"
    private const val RPM = "rpm"
    private const val GEAR = "gear"
    private const val INSTANT_BOOST_READY = "instant_boost_ready"
    private const val INSTANT_BOOST_ENABLED = "instant_boost_enabled"
    private const val DUAL_BOOST_ACTIVE = "dual_boost_active"
    private const val DUAL_BOOST_CHARGING = "dual_boost_charging"
    private const val DRAFT_ACTIVE = "draft_active"
    private const val DRAFT_CHARGING = "draft_charging"
    private const val EXCEED_GAUGE = "exceed_gauge"
    private const val FUSION_ACTIVE = "fusion_active"
    private const val CHARGER_GAUGE = "charger_gauge"
    private const val ERS = "ers"
    private const val TURBO_GAUGE = "turbo_gauge"
    private const val RACING = "racing"
    private const val ELAPSED_TIME = "elapsed_time"
    private const val CURRENT_LAP = "current_lap"
    private const val MAX_LAP = "max_lap"
    private const val BEST_LAP_TIME = "best_lap_time"
}
