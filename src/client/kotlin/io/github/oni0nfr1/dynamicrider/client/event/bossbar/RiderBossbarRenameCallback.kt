package io.github.oni0nfr1.dynamicrider.client.event.bossbar

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import net.minecraft.network.chat.Component
import java.util.UUID

fun interface RiderBossbarRenameCallback {
    fun handle(
        uuid: UUID,
        name: Component
    ): HandleResult

    companion object {
        @JvmField
        val EVENT = RiderEvent(ResourceStore.logger) { listeners, callSafely ->
            RiderBossbarRenameCallback { uuid, name ->
                for (listener in listeners) {
                    callSafely(listener) { listener.handle(uuid, name) }
                }
                HandleResult.PASS
            }
        }
    }
}