package io.github.oni0nfr1.dynamicrider.client.rider.sidebar

import io.github.oni0nfr1.dynamicrider.client.event.RiderPlayerInfoRemoveCallback
import io.github.oni0nfr1.dynamicrider.client.event.RiderPlayerInfoUpdateCallback
import io.github.oni0nfr1.dynamicrider.client.event.RiderRankingUpdateCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.state.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.state.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.rider.RiderBackend
import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.world.level.GameType
import java.util.UUID

class KartRankingManager(
    override val stateManager: HudStateManager
) : RiderBackend, AutoCloseable {

    data class Racer(
        val uuid: UUID,
        val name: String,
    )

    enum class ElimReason { DISCONNECT, SPECTATOR, OTHER }

    data class RankingEntry(
        val rank: Int,
        val racer: Racer,
        val sidebarValue: Int,
        val displayName: net.minecraft.network.chat.Component,
    )

    val racers: MutableState<LinkedHashMap<UUID, Racer>>
        = mutableStateOf(stateManager, linkedMapOf())
    val eliminated: MutableState<LinkedHashMap<UUID, ElimReason>>
        = mutableStateOf(stateManager, linkedMapOf())
    val alive: MutableState<LinkedHashSet<UUID>>
        = mutableStateOf(stateManager, linkedSetOf())

    val ranking: MutableState<List<RankingEntry>>
        = mutableStateOf(stateManager, emptyList())
    val isTimeAttack: MutableState<Boolean>
        = mutableStateOf(stateManager, true)

    private val sidebarListener = RiderRankingUpdateCallback.EVENT.register { sidebar ->
        updateRanking(sidebar)
        HandleResult.PASS
    }

    private val playerRemoveListener = RiderPlayerInfoRemoveCallback.EVENT.register { profileIds ->
        for (id in profileIds) {
            markEliminated(id, ElimReason.DISCONNECT)
        }
        HandleResult.PASS
    }

    private val playerUpdateListener = RiderPlayerInfoUpdateCallback.EVENT.register { packet ->
        if (!packet.actions().contains(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE))
            return@register HandleResult.PASS

        for (e in packet.entries()) {
            if (e.gameMode() == GameType.SPECTATOR) {
                markEliminated(e.profileId(), ElimReason.SPECTATOR)
            }
        }
        HandleResult.PASS
    }

    init {
        startRace(captureParticipantsNow())
        val snapshot = SidebarSnapshot.fromMcClient()
            ?: error("[KartRankingManager] Fatal: invalid instantiation detected")
        updateRanking(snapshot)
    }

    fun startRace(participants: Collection<Racer>) {
        racers.mutate {
            clear()
            for (p in participants) put(p.uuid, p)
        }
        eliminated.mutate { clear() }
        alive.mutate {
            clear()
            for (p in participants) add(p.uuid)
        }

        ranking.set(emptyList())
    }

    fun endRace() {
        racers.mutate { clear() }
        eliminated.mutate { clear() }
        alive.mutate { clear() }
        ranking.set(emptyList())
    }

    fun markEliminated(uuid: UUID, reason: ElimReason) {
        val racersMap = racers.value
        if (!racersMap.containsKey(uuid)) return

        if (eliminated.value.containsKey(uuid)) return

        eliminated.mutateIfChanged {
            put(uuid, reason) == null
        }

        alive.mutateIfChanged { remove(uuid) }
    }

    fun updateRanking(snapshot: SidebarSnapshot) {
        val nameToUuid = buildOnlineNameToUuidMap()

        val aliveSet = alive.value
        val racersMap = racers.value
        val prev = ranking.value

        val temp = ArrayList<RankingEntry>(snapshot.lines.size)
        var rank = 1
        for (line in snapshot.lines) {
            val uuid = nameToUuid[line.owner] ?: continue
            if (uuid !in aliveSet) continue

            val racer = racersMap[uuid] ?: continue
            temp.add(
                RankingEntry(
                    rank = rank++,
                    racer = racer,
                    sidebarValue = line.value,
                    displayName = line.name,
                )
            )
        }

        if (!sameRanking(prev, temp)) {
            ranking.set(temp)
        }
        if (temp.isEmpty()) isTimeAttack.set(true)
        else                isTimeAttack.set(false)
    }

    private fun sameRanking(a: List<RankingEntry>, b: List<RankingEntry>): Boolean {
        if (a.size != b.size) return false
        for (i in a.indices) {
            if (a[i].racer.uuid != b[i].racer.uuid) return false
            if (a[i].sidebarValue != b[i].sidebarValue) return false
        }
        return true
    }

    private fun buildOnlineNameToUuidMap(): Map<String, UUID> {
        val client = Minecraft.getInstance()
        val connection = client.connection ?: return emptyMap()

        val map = HashMap<String, UUID>()

        for (info in connection.onlinePlayers) {
            val profile = info.profile
            map[profile.name] = profile.id
        }
        return map
    }

    private fun captureParticipantsNow(): List<Racer> {
        val client = Minecraft.getInstance()
        val connection = client.connection ?: return emptyList()

        return connection.onlinePlayers
            .filter {
                it.gameMode != GameType.SPECTATOR
            }
            .map {
                val p = it.profile
                Racer(p.id, p.name)
            }
    }

    override fun close() {
        sidebarListener.close()
        playerRemoveListener.close()
        playerUpdateListener.close()
        endRace()
    }
}
