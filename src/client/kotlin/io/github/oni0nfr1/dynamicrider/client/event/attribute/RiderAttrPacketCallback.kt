package io.github.oni0nfr1.dynamicrider.client.event.attribute

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.impl.RiderEvent
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import net.minecraft.client.Minecraft
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.ai.attributes.Attributes

fun interface RiderAttrPacketCallback {
    fun handle(entity: Entity, modifiers: HashMap<ResourceLocation, Double>): HandleResult

    companion object {
        @JvmStatic
        fun invokeEvent(packet: ClientboundUpdateAttributesPacket) {
            val client = Minecraft.getInstance()

            val entityId = packet.entityId
            val entity = client.level?.getEntity(entityId) ?: return
            val attr = packet.values.find { snapshot ->
                snapshot.attribute == Attributes.EXPLOSION_KNOCKBACK_RESISTANCE
            } ?: return

            // 이후 이벤트 리스너에서 빠르게 읽을 수 있게 HashMap으로 변환
            val modifiersMap = hashMapOf<ResourceLocation, Double>()
            attr.modifiers.forEach { modifier ->
                modifiersMap[modifier.id] = modifier.amount
            }
            EXPLOSION_KNOCKBACK_RESISTANCE.invoker().handle(entity, modifiersMap)
        }

        @JvmField
        val EXPLOSION_KNOCKBACK_RESISTANCE = RiderEvent(ResourceStore.logger) { listeners, callSafely ->
            RiderAttrPacketCallback { entity, modifiers ->
                for (listener in listeners) {
                    callSafely(listener) { listener.handle(entity, modifiers) }
                }
                HandleResult.PASS
            }
        }
    }
}