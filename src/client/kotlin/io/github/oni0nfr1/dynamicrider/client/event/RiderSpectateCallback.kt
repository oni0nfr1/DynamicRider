package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

fun interface RiderSpectateCallback {

    /**
     * 플레이어가 관전자인 상태에서 관전 대상 엔티티를 바꿀 경우 호출. (setter HEAD)
     * 다른 엔티티 관전 상태에서 빠져나올 때도 호춯됨.
     *
     * 반환값:
     * - SUCCESS: 더 이상 다른 리스너는 호출하지 않음
     * - PASS: 다음 리스너로 넘김 (마지막까지 PASS면 PASS)
     */
    fun handle(
        player: Player,
        target: Entity
    ): HandleResult

    companion object {
        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderSpectateCallback { player, target ->
                for (listener in listeners) {
                    val result = callSafely(listener) { listener.handle(player, target) }
                    if (result == HandleResult.SUCCESS)
                        return@RiderSpectateCallback HandleResult.SUCCESS
                }
                HandleResult.PASS
            }
        }
    }
}