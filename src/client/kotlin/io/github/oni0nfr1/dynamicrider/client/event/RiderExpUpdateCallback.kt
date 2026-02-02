package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult

fun interface RiderExpUpdateCallback {
    fun handle(
        progress: Float,
        level: Int,
        total: Int,
    ): HandleResult

    companion object {
        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderExpUpdateCallback {
                progress, level, total ->
                for (listener in listeners) {
                    callSafely(listener) { listener.handle(progress, level, total) }
                }
                HandleResult.PASS
            }
        }
    }
}