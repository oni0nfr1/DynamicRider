package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult

fun interface RiderRaceEndCallback {
    fun handle(): HandleResult

    companion object {

        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderRaceStartCallback {
                ResourceStore.logger.info("[DynamicRider] Race Ended.")
                for (listener in listeners) {
                    val result = callSafely(listener) { listener.handle() }
                    when (result) {
                        HandleResult.FAILURE -> return@RiderRaceStartCallback HandleResult.FAILURE
                        HandleResult.SUCCESS -> return@RiderRaceStartCallback HandleResult.SUCCESS
                        else -> continue
                    }
                }
                HandleResult.PASS
            }
        }
    }
}