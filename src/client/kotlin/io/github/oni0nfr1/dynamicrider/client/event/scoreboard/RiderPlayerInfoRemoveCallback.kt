package io.github.oni0nfr1.dynamicrider.client.event.scoreboard

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import java.util.UUID

/**
 * 플레이어 목록에서 제거되는 패킷 수신 시 호출됨.
 *
 * 반환값:
 * - SUCCESS: 더 이상 다른 리스너는 호출하지 않음
 * - PASS: 다음 리스너로 넘김 (마지막까지 PASS면 PASS)
 */
fun interface RiderPlayerInfoRemoveCallback {

    fun handle(profileIds: List<UUID>): HandleResult

    companion object {
        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderPlayerInfoRemoveCallback { profileIds ->
                for (listener in listeners) {
                    val result = callSafely(listener) { listener.handle(profileIds) }
                    if (result == HandleResult.SUCCESS)
                        return@RiderPlayerInfoRemoveCallback HandleResult.SUCCESS
                }
                HandleResult.PASS
            }
        }
    }
}