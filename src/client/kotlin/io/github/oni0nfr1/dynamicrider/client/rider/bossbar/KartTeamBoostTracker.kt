package io.github.oni0nfr1.dynamicrider.client.rider.bossbar

import io.github.oni0nfr1.dynamicrider.client.event.bossbar.RiderBossbarAddCallback
import io.github.oni0nfr1.dynamicrider.client.event.bossbar.RiderBossbarProgressCallback
import io.github.oni0nfr1.dynamicrider.client.event.bossbar.RiderBossbarRemoveCallback
import io.github.oni0nfr1.dynamicrider.client.event.bossbar.RiderBossbarRenameCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.state.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.rider.RiderBackend
import java.util.UUID

class KartTeamBoostTracker(
    override val stateManager: HudStateManager
) : RiderBackend, AutoCloseable {

    companion object {
        private const val TEAM_NITRO_TITLE = "TEAM NITRO"
    }

    private var teamBoostBossbarId: UUID? = null
        set (value) {
            field = value
            teamBoostGaugeExists.set(field != null)
            gauge.set(0f)
        }

    val teamBoostGaugeExists = mutableStateOf(stateManager, false)
    val gauge = mutableStateOf(stateManager, 0f)

    private val newBossbarListener = RiderBossbarAddCallback.EVENT.register { uuid, name, progress ->
        val nameRaw = name.string
        if (nameRaw == TEAM_NITRO_TITLE) {
            teamBoostBossbarId = uuid
            gauge.set(progress)
        }
        HandleResult.PASS
    }

    private val removeBossbarListener = RiderBossbarRemoveCallback.EVENT.register { uuid ->
        if (uuid == teamBoostBossbarId) teamBoostBossbarId = null
        HandleResult.PASS
    }

    private val updateBossbarListener = RiderBossbarProgressCallback.EVENT.register { uuid, progress ->
        if (uuid == teamBoostBossbarId) gauge.set(progress)
        HandleResult.PASS
    }

    private val renameBossbarListener = RiderBossbarRenameCallback.EVENT.register { uuid, name ->
        val nameRaw = name.string
        if (uuid == teamBoostBossbarId && nameRaw != TEAM_NITRO_TITLE) teamBoostBossbarId = null
        if (uuid != teamBoostBossbarId && nameRaw == TEAM_NITRO_TITLE) teamBoostBossbarId = uuid
        HandleResult.PASS
    }

    override fun close() {
        newBossbarListener.close()
        removeBossbarListener.close()
        updateBossbarListener.close()
        renameBossbarListener.close()
    }
}