package io.github.oni0nfr1.dynamicrider.client.event.bossbar

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import java.util.UUID

fun interface RiderBossbarRemoveCallback {
    fun handle(uuid: UUID): HandleResult

    companion object {
        @JvmField
        val EVENT = RiderEvent(ResourceStore.logger) { listeners, callSafely ->
            RiderBossbarRemoveCallback { uuid ->
                for (listener in listeners) {
                    callSafely(listener) { listener.handle(uuid) }
                }
                HandleResult.PASS
            }
        }
    }
}