package io.github.oni0nfr1.dynamicrider.client.rider.sidebar

import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.mutableStateOf
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.level.GameType
import java.util.UUID

object RankingManager {

    data class Racer(
        val uuid: UUID,
        val name: String,
    )

    enum class ElimReason { DISCONNECT, SPECTATOR, OTHER }

    data class RankingEntry(
        val rank: Int,
        val racer: Racer,
        val sidebarValue: Int,      // 스코어(정렬에 쓰인 값)
        val displayName: Component, // 팀 포맷 등 적용된 표시용
    )

    var enabled: Boolean = false
        set(value) {
            if (field == value) return
            field = value

            if (value) {
                val participants = captureParticipantsNow()
                startRace(participants)
            } else {
                endRace()
            }
        }

    lateinit var stateManager: HudStateManager
    lateinit var racers: MutableState<LinkedHashMap<UUID, Racer>>          // 초기 참가자
    lateinit var eliminated: MutableState<LinkedHashMap<UUID, ElimReason>> // 탈락자(이유 기록)
    lateinit var alive: MutableState<LinkedHashSet<UUID>>                  // 탈락하지 않은 참가자

    lateinit var ranking: MutableState<List<RankingEntry>>
    lateinit var isTimeAttack: MutableState<Boolean>

    fun init(stateManager: HudStateManager) {
        this.stateManager = stateManager
        this.racers = mutableStateOf(stateManager, linkedMapOf())
        this.eliminated = mutableStateOf(stateManager, linkedMapOf())
        this.alive = mutableStateOf(stateManager, linkedSetOf())
        this.ranking = mutableStateOf(stateManager, emptyList())
        this.isTimeAttack = mutableStateOf(stateManager, false)
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
        if (!enabled) return

        val racersMap = racers.value
        if (!racersMap.containsKey(uuid)) return

        if (eliminated.value.containsKey(uuid)) return

        eliminated.mutateIfChanged {
            put(uuid, reason) == null
        }

        alive.mutateIfChanged { remove(uuid) } // remove가 true면 변경
    }

    fun updateRanking(snapshot: SidebarProvider.SidebarSnapshot) {
        if (!enabled) return


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
        val mc = Minecraft.getInstance()
        val conn = mc.connection ?: return emptyMap()

        val map = HashMap<String, UUID>()

        for (info in conn.onlinePlayers) {
            val profile = info.profile
            map[profile.name] = profile.id
        }
        return map
    }

    fun captureParticipantsNow(): List<Racer> {
        val mc = Minecraft.getInstance()
        val conn = mc.connection ?: return emptyList()

        return conn.onlinePlayers
            .filter {
                it.gameMode != GameType.SPECTATOR
            }
            .map { info ->
                val p = info.profile
                Racer(p.id, p.name)
            }
    }
}


/*
랭킹 관리 방식
//////////////////////////////////////////////////////////
가장 기본적인 방법
초기 참가자들 리스트: 레이스 시작 시점에서 관전 상태가 아닌 접속자들
현재 랭킹 리스트: 현재 읽혀져 있는 사이드바 랭킹 리스트

탈락자들은 초기 참가자들 중 현재 랭킹에 있지 않은 사람들... 이면 단순한 방식
//////////////////////////////////////////////////////////
조금 더 정확한 방식을 원한다면 다음 방식이 좋음
초기 참가자들 리스트: 레이스 시작 시점에서 직접적으로 카트에 타고 있었던 유저들
탈락자 리스트: 초기 참가자들 중 접속 중이 아니거나 관전 상태가 된 유저들
현재 살아남은 유저 리스트: 초기 참가자에서 탈락자를 제외한 유저들

랭킹 리스트: 살아남은 유저들을 사이드바 랭킹 순서대로 정렬한 것
////////////////////////////////////////////////////////////

랭킹을 언제 리프레시하는가
최대한 깔끔한 동작을 위해서는 필요한 상황에만 감지하도록 설계하는 게 좋음

그래도 단순함을 원한다면 매 틱마다 작동하는 방식을 쓸 순 있음
그러나 이 구조는 내가 설계하는 방식과는 살짝 맞지 않음

믹스인을 적극적으로 사용하면, 매 틱마다 해야 하는 연산의 수를 줄일 수 있음
가장 적절한 예가 사이드바 리프레시 패킷이 올 때만 사이드바를 읽어주는 방식
하지만 방식은 그리 간단하지 않음

////////////////////////////////////////////////////////////
사이드바 리프레시 패킷이 올 때는 살아남은 유저 리스트를 등수 순서대로 재졍렬함

엔티티 제거 패킷이 올 때는 그 엔티티가 살아남은 유저 리스트에 속할 경우 탈락자 리스트로 옮김
게임모드 변경 패킷이 올 때도 그 엔티티가 살아남은 유저 리스트에 속할 경우 탈락자 리스트로 옮김
////////////////////////////////////////////////////////////

*/