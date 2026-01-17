package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.animal.Cod

typealias Kart = Cod

/**
* 플레이어 탑승 패킷에서 탑승 대상이 마크라이더 카트일 경우 호출 (handle TAIL).
*
* 반환값:
* - SUCCESS: 더 이상 다른 리스너는 호출하지 않음
* - PASS: 다음 리스너로 넘김 (마지막까지 PASS면 PASS)
*/
fun interface RiderMountCallback {

    fun handle(
        kart: Kart,
        entity: Array<Entity?>,
    ): HandleResult

    companion object {
        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderMountCallback { kart, entity ->
                for (listener in listeners) {
                    val result = callSafely(listener) { listener.handle(kart, entity) }
                    if (result == HandleResult.SUCCESS)
                        return@RiderMountCallback HandleResult.SUCCESS
                }
                HandleResult.PASS
            }
        }
    }
}