package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket
import net.minecraft.world.InteractionResult

fun interface ActionBarTextCallback {
    /**
     * 액션바 텍스트 패킷을 받았을 때 호출됨 (바닐라 처리 직전).
     *
     * 반환값:
     * - SUCCESS: 더 이상 다른 리스너는 호출하지 않고, 바닐라 처리는 그대로 진행
     * - PASS: 다음 리스너로 넘김 (마지막까지 PASS면 PASS)
     * - FAIL: 더 이상 다른 리스너는 호출하지 않고, 바닐라 처리를 취소(액션바 표시 안 함)
     */
    fun interact(
        packetListener: ClientPacketListener,
        packet: ClientboundSetActionBarTextPacket
    ): InteractionResult

    companion object {

        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            ActionBarTextCallback { packetListener, packet ->
                for (listener in listeners) {
                    val r = callSafely(listener) { listener.interact(packetListener, packet) }
                    if (r != InteractionResult.PASS) return@ActionBarTextCallback r
                }
                InteractionResult.PASS
            }
        }
    }
}