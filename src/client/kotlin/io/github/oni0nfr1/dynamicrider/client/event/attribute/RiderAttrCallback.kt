package io.github.oni0nfr1.dynamicrider.client.event.attribute

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import net.minecraft.resources.ResourceLocation

fun interface RiderAttrCallback {
    fun handle(value: Double): HandleResult

    companion object {
        @JvmField val KART_ENGINE = Event(
            RiderAttrPacketCallback.EXPLOSION_KNOCKBACK_RESISTANCE,
            ResourceLocation.withDefaultNamespace("kart-engine")
        )
        @JvmField val KART_PERFORMANCE_LIMIT_LEVEL = Event(
            RiderAttrPacketCallback.EXPLOSION_KNOCKBACK_RESISTANCE,
            ResourceLocation.withDefaultNamespace("kart-perforce-limit-level")
        )
        @JvmField val MAX_LAP = Event(
            RiderAttrPacketCallback.EXPLOSION_KNOCKBACK_RESISTANCE,
            ResourceLocation.withDefaultNamespace("max-lap")
        )
        @JvmField val DUALBOOST_STATE = Event(
            RiderAttrPacketCallback.EXPLOSION_KNOCKBACK_RESISTANCE,
            ResourceLocation.withDefaultNamespace("dualboost-state")
        )
        @JvmField val KART_ENGINE_REAL = Event(
            RiderAttrPacketCallback.EXPLOSION_KNOCKBACK_RESISTANCE,
            ResourceLocation.withDefaultNamespace("kart-engine-real")
        )
        @JvmField val ACTIVE_INSTANT_BOOST = Event(
            RiderAttrPacketCallback.EXPLOSION_KNOCKBACK_RESISTANCE,
            ResourceLocation.withDefaultNamespace("active-instant-boost")
        )
        @JvmField val IS_DRIFTING = Event(
            RiderAttrPacketCallback.EXPLOSION_KNOCKBACK_RESISTANCE,
            ResourceLocation.withDefaultNamespace("is-drifting")
        )
    }

    class Event(
        parentEvent: RiderEvent<RiderAttrPacketCallback>,
        val modifierId: ResourceLocation
    ): RiderEvent<RiderAttrCallback>(
        logger = ResourceStore.logger,
        invokerFactory = { listeners, callSafely ->
            RiderAttrCallback { value ->
                for (listener in listeners) {
                    callSafely(listener) { listener.handle(value) }
                }
                HandleResult.PASS
            }
        }
    ), AutoCloseable {
        var currentValue: Double? = null

        private val packetListener = parentEvent.register { modifiers ->
            val amount = modifiers[modifierId] ?: return@register HandleResult.PASS
            if (currentValue != amount) {
                currentValue = amount
                invoker().handle(amount)
            }
            HandleResult.PASS
        }

        override fun close() {
            packetListener.close()
        }
    }
}