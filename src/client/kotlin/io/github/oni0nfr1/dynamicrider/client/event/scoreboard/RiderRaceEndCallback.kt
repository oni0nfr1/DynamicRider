package io.github.oni0nfr1.dynamicrider.client.event.scoreboard

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.util.debugLog

/**
 * 레이싱(타임어택, 라이센스, 멀티플레이 모두 포함)이 종료되었을 때 호출.
 *
 * timerdisplay Objective가 사이드바에서 지워지는 타이밍에 호출됨.
 * (정확히는 timerdisplay가 사이드바에서 내려가거나, Objective 자체가 지워질 때 그 직전)
 *
 * 반환값:
 * - PASS: 다음 리스너로 넘김 (PASS만 반환함)
 */
fun interface RiderRaceEndCallback {
    fun handle(reason: RaceEndReason): HandleResult

    companion object {

        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderRaceEndCallback { reason ->
                when (reason) {
                    RaceEndReason.DISCONNECT -> debugLog("Race ended due to client disconnection")
                    RaceEndReason.FINISH -> debugLog("Race Ended.")
                    RaceEndReason.OTHER -> debugLog("Race Ended by unknown reason.")
                }
                for (listener in listeners) {
                    callSafely(listener) { listener.handle(reason) }
                }
                HandleResult.PASS
            }
        }
    }

    enum class RaceEndReason {
        DISCONNECT,
        FINISH,
        OTHER,
    }
}