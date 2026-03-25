package io.github.oni0nfr1.dynamicrider.client.event.attribute

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.util.isClientPlayerId
import net.minecraft.resources.ResourceLocation

fun interface RiderAttrCallback {
    fun handle(entityId: Int, value: Double): HandleResult

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
            RiderAttrCallback { entityId, value ->
                for (listener in listeners) {
                    callSafely(listener) { listener.handle(entityId, value) }
                }
                HandleResult.PASS
            }
        }
    ), AutoCloseable {
        val valueByEntityId: HashMap<Int, Double> = hashMapOf()
        var myValue: Double? = null

        private val packetListener = parentEvent.register { entity, modifiers ->
            val entityId = entity.id
            if (isClientPlayerId(entityId)) updateMine(entityId, modifiers)

            val amount = modifiers[modifierId] ?: return@register HandleResult.PASS
            if (valueByEntityId[entityId] != amount) {
                valueByEntityId[entityId] = amount
                invoker().handle(entityId, amount)
            }
            HandleResult.PASS
        }

        private fun updateMine(entityId: Int, modifiers : Map<ResourceLocation, Double>) {
            val amount = modifiers[modifierId] ?: return
            if (myValue != amount) {
                myValue = amount
                invoker().handle(entityId, amount)
            }
            HandleResult.PASS
        }

        override fun close() {
            packetListener.close()
        }
    }
}