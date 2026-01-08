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
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) return;

        int vehicleId = packet.getVehicle();
        int[] passengerIds = packet.getPassengers();

        Entity[] passengers = new Entity[passengerIds.length];
        for (int i = 0; i < passengerIds.length; i++) {
            passengers[i] = level.getEntity(passengerIds[i]);
        }

        Entity vehicle = client.level.getEntity(vehicleId);
        if (vehicle instanceof Cod) {
            RiderMountCallback.EVENT.invoker().handle((Cod) vehicle, passengers);
        }
    }

    @Inject(method = "handleRemoveEntities", at = @At("TAIL"))
    private void dynrider$onRemoveEntities(
        ClientboundRemoveEntitiesPacket packet,
        CallbackInfo ci
    ) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) return;

        for (int id : packet.getEntityIds()) {
            Entity entity = level.getEntity(id);

            if (entity instanceof Cod) {
                RiderMountCallback.EVENT.invoker().handle((Cod) entity, new Entity[0]);
            }
        }
    }

}
