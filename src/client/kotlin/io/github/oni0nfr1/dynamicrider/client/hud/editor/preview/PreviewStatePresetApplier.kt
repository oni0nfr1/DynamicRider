package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType

/** 기본 preview context를 생성하고 선택한 preset의 값을 적용한다. */
object PreviewStatePresetApplier {
    /**
     * [stateType]의 새 context에 [preset]을 적용한다.
     *
     * 매번 새 context를 만들기 때문에 이전 preset이나 수동 편집 값이 남지 않는다.
     * 호환되지 않는 preset 또는 capability 값은 [IllegalArgumentException]으로 거부한다.
     */
    fun createContext(
        stateType: KartStateType<out KartState>,
        preset: PreviewStatePreset,
    ): PreviewHudSceneContext<out KartState> {
        val context = PreviewHudSceneContextFactory.create(stateType)
        apply(context, preset)
        return context
    }

    private fun apply(
        context: PreviewHudSceneContext<out KartState>,
        preset: PreviewStatePreset,
    ) {
        val state = context.kartState
        require(preset.isCompatibleWith(state)) {
            "Preview preset '${preset.id}' is incompatible with kart state type '${context.kartStateType.id}'"
        }

        applyKartValues(state, preset)
        context.raceState.applyValues(preset.race)
        context.previewRankingState.value = preset.ranking
    }

    private fun applyKartValues(
        state: KartState,
        preset: PreviewStatePreset,
    ) {
        val values = preset.kart

        values.speed?.let {
            state.requireCapability<PreviewSpeedKartState>(preset).speed = it
        }
        values.nitro?.let {
            state.requireCapability<PreviewNitroKartState>(preset).applyValues(it)
        }
        values.draft?.let {
            state.requireCapability<PreviewDraftKartState>(preset).applyValues(it)
        }
        values.exceedGauge?.let {
            state.requireCapability<PreviewV1KartState>(preset).exceedGauge = it
        }
        values.chargerGauge?.let {
            state.requireCapability<PreviewChargeKartState>(preset).chargerGauge = it
        }
    }

    private fun PreviewNitroKartState.applyValues(values: PreviewNitroValues) {
        isDrifting = values.isDrifting
        isBoosting = values.isBoosting
        maxBoost = values.maxBoost
        nitro = values.nitro
        nitroGauge = values.nitroGauge
        teamNitro = values.teamNitro
        teamBoostGaugeAvailable = values.teamBoostGaugeAvailable
        teamBoostGauge = values.teamBoostGauge
    }

    private fun PreviewDraftKartState.applyValues(values: PreviewDraftValues) {
        draftActive = values.active
        draftCharging = values.charging
    }

    private fun PreviewRaceState.applyValues(values: PreviewRaceValues) {
        racing = values.racing
        elapsedTimeMillis = values.elapsedTimeMillis
        currentLap = values.currentLap
        maxLap = values.maxLap
        bestLapTimeMillis = values.bestLapTimeMillis
    }

    private inline fun <reified S : PreviewKartState> KartState.requireCapability(
        preset: PreviewStatePreset,
    ): S = this as? S ?: throw IllegalArgumentException(
        "Preview preset '${preset.id}' requires ${S::class.java.simpleName}",
    )
}
