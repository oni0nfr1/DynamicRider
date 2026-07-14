package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.RankingState
import net.minecraft.network.chat.Component
import java.util.UUID

/** 편집기에서 제공하는 built-in preview preset registry다. */
object PreviewStatePresets {
    val IDLE = PreviewStatePreset(
        id = "idle",
        displayName = "Idle",
        requirement = PreviewPresetRequirement.SPEED,
        kart = PreviewKartValues(speed = 0.0),
        race = PreviewRaceValues(
            racing = false,
            elapsedTimeMillis = 0L,
            currentLap = 0,
            bestLapTimeMillis = null,
        ),
    )

    val RACING = PreviewStatePreset(
        id = "racing",
        displayName = "Racing",
        requirement = PreviewPresetRequirement.SPEED,
        kart = PreviewKartValues(speed = 169.9),
    )

    val DRIFTING = PreviewStatePreset(
        id = "drifting",
        displayName = "Drifting",
        requirement = PreviewPresetRequirement.NITRO_DRAFT,
        kart = PreviewKartValues(
            speed = 128.0,
            nitro = PreviewNitroValues(
                isDrifting = true,
                nitroGauge = 0.82f,
            ),
            draft = PreviewDraftValues(charging = true),
        ),
    )

    val BOOSTING = PreviewStatePreset(
        id = "boosting",
        displayName = "Boosting",
        requirement = PreviewPresetRequirement.NITRO,
        kart = PreviewKartValues(
            speed = 245.0,
            nitro = PreviewNitroValues(
                isBoosting = true,
                nitro = 1,
                nitroGauge = 0.25f,
            ),
        ),
    )

    val TEAM_BOOST = PreviewStatePreset(
        id = "team_boost",
        displayName = "Team Boost",
        requirement = PreviewPresetRequirement.NITRO,
        kart = PreviewKartValues(
            speed = 230.0,
            nitro = PreviewNitroValues(
                isBoosting = true,
                teamNitro = 1,
                teamBoostGaugeAvailable = true,
                teamBoostGauge = 0.78f,
            ),
        ),
    )

    val FINAL_LAP = PreviewStatePreset(
        id = "final_lap",
        displayName = "Final Lap",
        race = PreviewRaceValues(
            elapsedTimeMillis = 112_000L,
            currentLap = 3,
            maxLap = 3,
            bestLapTimeMillis = 54_320L,
        ),
    )

    val RANKING = PreviewStatePreset(
        id = "ranking",
        displayName = "Ranking",
        ranking = rankingState(),
    )

    val V1_EXCEED = PreviewStatePreset(
        id = "v1_exceed",
        displayName = "V1 Exceed",
        requirement = PreviewPresetRequirement.V1,
        kart = PreviewKartValues(
            speed = 220.0,
            nitro = PreviewNitroValues(isBoosting = true),
            draft = PreviewDraftValues(active = true),
            exceedGauge = 0.85f,
        ),
    )

    val CHARGE_GAUGE = PreviewStatePreset(
        id = "charge_gauge",
        displayName = "Charge Gauge",
        requirement = PreviewPresetRequirement.CHARGE,
        kart = PreviewKartValues(
            speed = 205.0,
            nitro = PreviewNitroValues(nitroGauge = 0.9f),
            draft = PreviewDraftValues(charging = true),
            chargerGauge = 0.75f,
        ),
    )

    val entries: List<PreviewStatePreset> = listOf(
        IDLE,
        RACING,
        DRIFTING,
        BOOSTING,
        TEAM_BOOST,
        FINAL_LAP,
        RANKING,
        V1_EXCEED,
        CHARGE_GAUGE,
    )

    private val byId = entries.associateBy(PreviewStatePreset::id).also {
        require(it.size == entries.size) { "Preview preset IDs must be unique" }
    }

    /** [id]로 built-in preset을 조회한다. */
    fun byId(id: String): PreviewStatePreset? = byId[id]

    /** [state]에 적용 가능한 built-in preset만 반환한다. */
    fun compatibleWith(state: KartState): List<PreviewStatePreset> =
        entries.filter { it.isCompatibleWith(state) }

    private fun rankingState(): RankingState.Available {
        val racers = listOf(
            racer("00000000-0000-0000-0000-000000000001", "Player"),
            racer("00000000-0000-0000-0000-000000000002", "Rider 2"),
            racer("00000000-0000-0000-0000-000000000003", "Rider 3"),
            racer("00000000-0000-0000-0000-000000000004", "Rider 4"),
        )
        val entries = racers.mapIndexed { index, racer ->
            RankingState.Entry(
                rank = index + 1,
                racer = racer,
                sidebarValue = 10_000 - index * 250,
                displayName = Component.literal(racer.name),
            )
        }
        return RankingState.Available(
            entries = entries,
            racers = racers.associateBy(RankingState.Racer::uuid),
            alive = racers.mapTo(linkedSetOf(), RankingState.Racer::uuid),
            localRacerId = racers.first().uuid,
        )
    }

    private fun racer(uuid: String, name: String): RankingState.Racer =
        RankingState.Racer(UUID.fromString(uuid), name)
}
