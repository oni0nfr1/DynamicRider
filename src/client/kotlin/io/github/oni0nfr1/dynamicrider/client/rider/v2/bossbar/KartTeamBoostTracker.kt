package io.github.oni0nfr1.dynamicrider.client.rider.v2.bossbar

import io.github.oni0nfr1.dynamicrider.client.event.bossbar.RiderBossbarAddCallback
import io.github.oni0nfr1.dynamicrider.client.event.bossbar.RiderBossbarProgressCallback
import io.github.oni0nfr1.dynamicrider.client.event.bossbar.RiderBossbarRemoveCallback
import io.github.oni0nfr1.dynamicrider.client.event.bossbar.RiderBossbarRenameCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.v2.RiderBackend
import java.util.UUID

object KartTeamBoostTracker : RiderBackend() {
    private const val TEAM_NITRO_TITLE = "TEAM NITRO"

    override fun onRaceStart() { reset() }

    override fun onRaceEnd() { reset() }

    override fun init() {
        RiderBossbarAddCallback.EVENT.register { uuid, name, progress ->
            if (name.string == TEAM_NITRO_TITLE) {
                teamBoostBossbarId = uuid
                gauge = progress
            }
            HandleResult.PASS
        }

        RiderBossbarRemoveCallback.EVENT.register { uuid ->
            if (uuid == teamBoostBossbarId) {
                teamBoostBossbarId = null
            }
            HandleResult.PASS
        }

        RiderBossbarProgressCallback.EVENT.register { uuid, progress ->
            if (uuid == teamBoostBossbarId) {
                gauge = progress
            }
            HandleResult.PASS
        }

        RiderBossbarRenameCallback.EVENT.register { uuid, name ->
            val nameRaw = name.string
            if (uuid == teamBoostBossbarId && nameRaw != TEAM_NITRO_TITLE) {
                teamBoostBossbarId = null
            }
            if (uuid != teamBoostBossbarId && nameRaw == TEAM_NITRO_TITLE) {
                teamBoostBossbarId = uuid
            }
            HandleResult.PASS
        }
    }

    private var teamBoostBossbarId: UUID? = null
        set(value) {
            field = value
            gaugeExists = field != null
            if (field == null) {
                gauge = 0f
            }
        }

    var gaugeExists: Boolean = false
        private set

    var gauge: Float = 0f
        private set

    private fun reset() {
        teamBoostBossbarId = null
    }
}
