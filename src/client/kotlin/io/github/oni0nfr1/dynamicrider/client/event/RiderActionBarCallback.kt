package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.chat.Component

fun interface RiderActionBarCallback {
    /**
     * 마크라이더 타코미터 액션바 텍스트 패킷을 받았을 때 호출됨 (WrapOperation, 액션바 렌더 직전).
     *
     * 반환값:
     * - SUCCESS: 더 이상 다른 리스너는 호출하지 않고, 바닐라 처리는 그대로 진행
     * - PASS: 다음 리스너로 넘김 (마지막까지 PASS면 PASS)
     * - FAIL: 더 이상 다른 리스너는 호출하지 않고, 바닐라 처리를 취소(액션바 표시 안 함)
     */
    fun handle(
        packetListener: ClientPacketListener,
        text: Component,
        raw: String,
    ): HandleResult

    companion object {

        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderActionBarCallback { packetListener, text, raw ->
                for (listener in listeners) {
                    val result = callSafely(listener) { listener.handle(packetListener, text, raw) }
                    when (result) {
                        HandleResult.FAILURE -> return@RiderActionBarCallback HandleResult.FAILURE
                        HandleResult.SUCCESS -> return@RiderActionBarCallback HandleResult.SUCCESS
                        else -> continue
                    }
                }
                HandleResult.PASS
            }
        }
    }
}