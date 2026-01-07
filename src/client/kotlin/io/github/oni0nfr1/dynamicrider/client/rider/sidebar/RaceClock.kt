package io.github.oni0nfr1.dynamicrider.client.rider.sidebar

import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.state.MutableState
import io.github.oni0nfr1.dynamicrider.client.rider.RaceTime
import io.github.oni0nfr1.dynamicrider.client.util.schedule.Ticker

object RaceClock {

    lateinit var stateManager: HudStateManager

    /** 깊은 복사를 이용해야 함 */
    lateinit var time: MutableState<RaceTime>
    lateinit var racing: MutableState<Boolean>

    // 어차피 매 틱마다 바뀌는 거 폴링으로 해 버리는 게 맞음
    var pollTask: Ticker.TaskHandle? = null

    var enabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                pollTask = Ticker.runTaskTimer(1, 20,
                    this::poll
                )
            } else {
                pollTask?.cancel()
            }
        }

    fun init(stateManager: HudStateManager) {
        this.stateManager = stateManager
        time = MutableState(stateManager, RaceTime())
        racing = MutableState(stateManager, false)
    }

    fun poll() {
        val sidebarTitle = SidebarProvider.readSidebarTitle()
        if (sidebarTitle == null) {
            racing.set(false)
            return
        }

        racing.set(true)
        time.mutateIfChanged {
            try {
                update(sidebarTitle)
                return@mutateIfChanged true
            } catch (err: Exception) {
                return@mutateIfChanged false
            }
        }
    }

}