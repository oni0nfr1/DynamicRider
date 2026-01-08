package io.github.oni0nfr1.dynamicrider.client.event

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import net.minecraft.network.chat.Component

fun interface RiderSetSidebarTitleCallback {

    fun handle(title: Component, raw: String): HandleResult

    companion object {
        @JvmField
        val EVENT = RiderEvent(logger = ResourceStore.logger) { listeners, callSafely ->
            RiderSetSidebarTitleCallback { title, raw ->
                for (listener in listeners) {
                    val result = callSafely(listener) { listener.handle(title, raw) }
                    if (result == HandleResult.SUCCESS)
                        return@RiderSetSidebarTitleCallback HandleResult.SUCCESS
                }
                HandleResult.PASS
            }
        }
    }
}