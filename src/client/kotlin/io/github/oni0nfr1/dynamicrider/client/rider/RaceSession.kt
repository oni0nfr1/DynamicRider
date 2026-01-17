package io.github.oni0nfr1.dynamicrider.client.rider

import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.KartRankingManager
import java.lang.AutoCloseable

/**
 * 마크라이더에서 "카트를 탄 상태"와 "레이싱 중인 상태"는 별개임.
 *
 * 레이싱 중에서도 카트에 내릴 수 있고 HUD가 바뀌어야 하는 상황이 생길 수 있는데,
 * 그런 상황에서 레이싱 전체 과정의 규모에서 수명주기를 가지는 데이터는 여기서 관리됨
 *
 * ex) 순위 감지 (경기 초반에 참가자/관전자들 감지하고 레이싱 중에 리타이어/연결 끊긴 사람들 집계)
 */
class RaceSession(stateManager: HudStateManager): AutoCloseable {
    val rankingManager = KartRankingManager(stateManager)

    override fun close() {
        rankingManager.close()
    }
}