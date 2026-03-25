package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.event.RiderMountCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class RiderMountMixin {

    @Inject(
        method = "handleSetEntityPassengersPacket",
        at = @At("TAIL")
    )
    private void dynrider$onKartMount(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        // 탑승 패킷이 올 경우 호출됩니다.
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) return;

        int vehicleId = packet.getVehicle();
        int[] passengerIds = packet.getPassengers();

        // 패킷에서 찾은 탑승자들을 스캔합니다.
        // ArrayList가 아닌 Array이며, 각 원소가 null일 수 있으므로 이벤트 수신부에서 체크가 필요할 수 있습니다.
        Entity[] passengers = new Entity[passengerIds.length];
        for (int i = 0; i < passengerIds.length; i++) {
            passengers[i] = level.getEntity(passengerIds[i]);
        }

        // 탑승 대상이 마크라이더 카트(대구) 일 경우 탑승 패킷 이벤트가 호출됩니다.
        Entity vehicle = client.level.getEntity(vehicleId);
        if (vehicle instanceof Cod) {
            RiderMountCallback.EVENT.invoker().handle((Cod) vehicle, passengers);
        }
    }

    @Inject(
            method = "handleRemoveEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void dynrider$onRemoveEntities(
        ClientboundRemoveEntitiesPacket packet,
        CallbackInfo ci
    ) {
        Minecraft client = Minecraft.getInstance();
        // 월드에서 엔티티를 제거하는 패킷이 수신될 경우 호출됩니다.
        ClientLevel level = client.level;
        if (level == null) return;

        // 제거된 엔티티가 마크라이더 카트일 경우 비어있는 Array로 탑승 패킷 이벤트가 호출됩니다.
        for (int id : packet.getEntityIds()) {
            Entity entity = level.getEntity(id);

            if (entity instanceof Cod) {
                RiderMountCallback.EVENT.invoker().handle((Cod) entity, new Entity[0]);
            }
        }
    }

}
