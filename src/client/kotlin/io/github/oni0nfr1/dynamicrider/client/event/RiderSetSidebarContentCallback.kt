package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.SidebarSnapshot

fun interface RiderSetSidebarContentCallback {

    fun handle(sidebar: SidebarSnapshot): HandleResult

    companion object {
        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderSetSidebarContentCallback { sidebar ->
                for (listener in listeners) {
                    val result = callSafely(listener) { listener.handle(sidebar) }
                    if (result == HandleResult.SUCCESS)
                        return@RiderSetSidebarContentCallback HandleResult.SUCCESS
                }
                HandleResult.PASS
            }
        }
    }
}