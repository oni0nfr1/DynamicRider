package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.chat.LapMessage
import io.github.oni0nfr1.dynamicrider.client.util.debugInfo

fun interface RiderLapFinishCallback {

    fun handle(msg: LapMessage) : HandleResult

    companion object {
        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderLapFinishCallback { msg ->
                ResourceStore.logger.debugInfo("[DynamicRider] Lap Finished")
                for (listener in listeners) { callSafely(listener) { listener.handle(msg) } }
                HandleResult.PASS
            }
        }
    }

}