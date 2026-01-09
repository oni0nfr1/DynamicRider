package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket

fun interface RiderPlayerInfoUpdateCallback {

    /**
     * 플레이어 정보 업데이트 패킷 수신 시 호출됨.
     *
     * 반환값:
     * - SUCCESS: 더 이상 다른 리스너는 호출하지 않음
     * - PASS: 다음 리스너로 넘김 (마지막까지 PASS면 PASS)
     */
    fun handle(packet: ClientboundPlayerInfoUpdatePacket): HandleResult

    companion object {
        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderPlayerInfoUpdateCallback { packet ->
                for (listener in listeners) {
                    val result = callSafely(listener) { listener.handle(packet) }
                    if (result == HandleResult.SUCCESS)
                        return@RiderPlayerInfoUpdateCallback HandleResult.SUCCESS
                }
                HandleResult.PASS
            }
        }
    }
}
