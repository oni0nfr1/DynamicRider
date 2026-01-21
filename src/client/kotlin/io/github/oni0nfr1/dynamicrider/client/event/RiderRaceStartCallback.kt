package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.util.debugLog

/**
 * 레이싱(타임어택, 라이센스, 멀티플레이 모두 포함)이 시작되었을 때 호출.
 *
 * timerdisplay Objective가 사이드바에 표시되는 타이밍에 호출됨.
 * (정확히는 timerdisplay 표시 패킷을 받고 Sidebar.setDisplayObjective()가 호출되기 직전 시점)
 *
 * 반환값:
 * - PASS: 다음 리스너로 넘김 (PASS만 반환함)
 */
fun interface RiderRaceStartCallback {
    fun handle(): HandleResult

    companion object {

        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderRaceStartCallback {
                debugLog("Race Start.")
                for (listener in listeners) { callSafely(listener) { listener.handle() } }
                HandleResult.PASS
            }
        }
    }
}