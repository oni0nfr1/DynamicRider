package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.util.schedule.Ticker
import net.minecraft.network.chat.Component

/**
 * 마인크래프트 timerdisplay 사이드바의 타이머 부분이 수정되었을 때 호출 (수정 패킷 수신한 틱 끝에 호출됨)
 *
 * 같은 틱에 이벤트가 여러 번 호출되어도 실제 호출은 가장 마지막 호출 한 번만 일어남
 *
 * invoker의 handle()은 항상 [HandleResult.PASS]를 반환.
 *
 * 반환값:
 * - [HandleResult.SUCCESS]: 더 이상 다른 리스너는 호출하지 않음
 * - [HandleResult.PASS]: 다음 리스너로 넘김
 */
fun interface RiderTimerUpdateCallback {

    fun handle(title: Component, raw: String): HandleResult

    companion object {
        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderTimerUpdateCallback { title, raw ->
                InvocationManager.latestSidebarTitle = title

                if (InvocationManager.dispatchScheduledThisTick)
                    return@RiderTimerUpdateCallback HandleResult.PASS

                InvocationManager.dispatchScheduledThisTick = true
                Ticker.runTaskLater(delay = 0) {
                    try {
                        val titleToDispatch = InvocationManager.latestSidebarTitle
                            ?: run {
                                InvocationManager.dispatchScheduledThisTick = false
                                return@runTaskLater
                            }

                        val rawToDispatch = titleToDispatch.string
                        for (listener in listeners) {
                            val result = callSafely(listener) {
                                listener.handle(titleToDispatch, rawToDispatch)
                            }
                            if (result == HandleResult.SUCCESS)
                                break
                        }
                    } finally {
                        InvocationManager.latestSidebarTitle = null
                        InvocationManager.dispatchScheduledThisTick = false
                    }
                }
                HandleResult.PASS
            }
        }
    }

    private object InvocationManager {
        var dispatchScheduledThisTick = false
        var latestSidebarTitle: Component? = null
    }
}