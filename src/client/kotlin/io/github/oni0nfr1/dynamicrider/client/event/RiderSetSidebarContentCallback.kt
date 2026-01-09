package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.SidebarSnapshot
import io.github.oni0nfr1.dynamicrider.client.util.schedule.Ticker

fun interface RiderSetSidebarContentCallback {

    /**
     * 마인크래프트 화면의 사이드바가 수정되었을 때 호출 (수정 패킷 수신한 틱 끝에 호출됨)
     *
     * 같은 틱에 이벤트가 여러 번 호출되어도 실제 호출은 가장 마지막 호출 한 번만 일어남
     *
     * invoker의 handle()은 항상 [HandleResult.PASS]를 반환.
     *
     * 반환값:
     * - [HandleResult.SUCCESS]: 더 이상 다른 리스너는 호출하지 않음
     * - [HandleResult.PASS]: 다음 리스너로 넘김
     *
     * **주의**
     * sidebar 객체는 mutable인데, 내부 값을 수정하면 이후 핸들러에 부작용이 일어날 수 있음
     */
    fun handle(sidebar: SidebarSnapshot): HandleResult

    companion object {
        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderSetSidebarContentCallback { sidebar ->
                InvocationManager.latestSidebarSnapshot = sidebar

                if (InvocationManager.dispatchScheduledThisTick)
                    return@RiderSetSidebarContentCallback HandleResult.PASS

                InvocationManager.dispatchScheduledThisTick = true
                Ticker.runTaskLater(delay = 0) {
                    try {
                        val snapshotToDispatch = InvocationManager.latestSidebarSnapshot
                            ?: run {
                                InvocationManager.dispatchScheduledThisTick = false
                                return@runTaskLater
                            }

                        for (listener in listeners) {
                            val result = callSafely(listener) { listener.handle(snapshotToDispatch) }
                            if (result == HandleResult.SUCCESS)
                                break
                        }
                    } finally {
                        InvocationManager.latestSidebarSnapshot = null
                        InvocationManager.dispatchScheduledThisTick = false
                    }
                }
                HandleResult.PASS
            }
        }
    }

    private object InvocationManager {
        var dispatchScheduledThisTick = false
        var latestSidebarSnapshot: SidebarSnapshot? = null
    }
}