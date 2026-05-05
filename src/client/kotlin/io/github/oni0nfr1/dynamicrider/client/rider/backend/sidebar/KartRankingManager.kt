package io.github.oni0nfr1.dynamicrider.client.rider.backend.sidebar

import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderPlayerInfoRemoveCallback
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderPlayerInfoUpdateCallback
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderRankingUpdateCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.SidebarSnapshot
import io.github.oni0nfr1.dynamicrider.client.rider.backend.RiderBackend
import io.github.oni0nfr1.dynamicrider.client.util.schedule.Ticker
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.world.level.GameType
import java.util.UUID

object KartRankingManager: RiderBackend() {
    data class Racer(
        val uuid: UUID,
        val name: String,
    )

    enum class ElimReason {
        DISCONNECT,
        SPECTATOR,
        OTHER,
    }

    data class RankingEntry(
        val rank: Int,
        val racer: Racer,
        val sidebarValue: Int,
        val displayName: Component,
    )

    override fun init() {
        if (raceActive) {
            onRaceStart()
        }

        RiderRankingUpdateCallback.EVENT.register { sidebar ->
            if (!raceActive || !initialized) {
                return@register HandleResult.PASS
            }

            updateRanking(sidebar)
            HandleResult.PASS
        }

        RiderPlayerInfoRemoveCallback.EVENT.register { profileIds ->
            if (!raceActive || !initialized) {
                return@register HandleResult.PASS
            }

            for (id in profileIds) {
                markEliminated(id, ElimReason.DISCONNECT)
            }
            HandleResult.PASS
        }

        RiderPlayerInfoUpdateCallback.EVENT.register { packet ->
            if (!raceActive || !initialized) {
                return@register HandleResult.PASS
            }

            if (!packet.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE)) {
                return@register HandleResult.PASS
            }

            for (entry in packet.entries()) {
                if (entry.gameMode() == GameType.SPECTATOR) {
                    markEliminated(entry.profileId(), ElimReason.SPECTATOR)
                }
            }
            HandleResult.PASS
        }
    }

    override fun onRaceStart() {
        raceActive = true
        initialized = false
        initTask?.cancel()
        clearState()

        initTask = Ticker.runTaskLater(3) {
            if (!raceActive) return@runTaskLater

            startRace(captureParticipantsNow())
            initialized = true
            SidebarSnapshot.fromMcClient()?.let(::updateRanking)
        }
    }

    override fun onRaceEnd() {
        raceActive = false
        initialized = false
        initTask?.cancel()
        initTask = null
        clearState()
    }

    private var raceActive: Boolean = isRaceActiveNow()
    private var initialized: Boolean = false
    private var initTask: Ticker.TaskHandle? = null

    var racers: LinkedHashMap<UUID, Racer> = linkedMapOf()
        private set
    var eliminated: LinkedHashMap<UUID, ElimReason> = linkedMapOf()
        private set
    var alive: LinkedHashSet<UUID> = linkedSetOf()
        private set
    var ranking: List<RankingEntry> = emptyList()
        private set
    var isTimeAttack: Boolean = true
        private set


    private fun startRace(participants: Collection<Racer>) {
        racers = LinkedHashMap<UUID, Racer>().apply {
            for (participant in participants) {
                put(participant.uuid, participant)
            }
        }
        eliminated = linkedMapOf()
        alive = LinkedHashSet<UUID>().apply {
            for (participant in participants) {
                add(participant.uuid)
            }
        }
        ranking = emptyList()
        isTimeAttack = true
    }

    private fun clearState() {
        racers = linkedMapOf()
        eliminated = linkedMapOf()
        alive = linkedSetOf()
        ranking = emptyList()
        isTimeAttack = true
    }

    private fun markEliminated(uuid: UUID, reason: ElimReason) {
        if (!racers.containsKey(uuid)) return
        if (eliminated.containsKey(uuid)) return

        eliminated = LinkedHashMap(eliminated).apply {
            put(uuid, reason)
        }
        alive = LinkedHashSet(alive).apply {
            remove(uuid)
        }
    }

    private fun updateRanking(snapshot: SidebarSnapshot) {
        val nameToUuid = buildOnlineNameToUuidMap()
        val temp = ArrayList<RankingEntry>(snapshot.lines.size)
        var rankNumber = 1

        for (line in snapshot.lines) {
            val uuid = nameToUuid[line.owner] ?: continue
            if (uuid !in alive) continue

            val racer = racers[uuid] ?: continue
            temp.add(
                RankingEntry(
                    rank = rankNumber++,
                    racer = racer,
                    sidebarValue = line.value,
                    displayName = line.name,
                ),
            )
        }

        if (!sameRanking(ranking, temp)) {
            ranking = temp
        }
        isTimeAttack = temp.isEmpty()
    }

    private fun sameRanking(a: List<RankingEntry>, b: List<RankingEntry>): Boolean {
        if (a.size != b.size) return false
        for (index in a.indices) {
            if (a[index].racer.uuid != b[index].racer.uuid) return false
            if (a[index].sidebarValue != b[index].sidebarValue) return false
        }
        return true
    }

    private fun buildOnlineNameToUuidMap(): Map<String, UUID> {
        val connection = Minecraft.getInstance().connection ?: return emptyMap()
        val map = HashMap<String, UUID>()

        for (info in connection.onlinePlayers) {
            val profile = info.profile
            map[profile.name] = profile.id
        }

        return map
    }

    private fun captureParticipantsNow(): List<Racer> {
        val connection = Minecraft.getInstance().connection ?: return emptyList()

        return connection.onlinePlayers
            .filter { it.gameMode != GameType.SPECTATOR }
            .map {
                val profile = it.profile
                Racer(profile.id, profile.name)
            }
    }

    private fun isRaceActiveNow(): Boolean {
        return try {
            DynamicRiderClient.instance.raceSession != null
        } catch (_: IllegalStateException) {
            false
        }
    }
}
