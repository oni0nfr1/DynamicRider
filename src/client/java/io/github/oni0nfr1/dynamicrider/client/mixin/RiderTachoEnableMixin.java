package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.rider.KartDetector;
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.RiderMountState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

// 타코미터 HUD를 생성해야 할지를 감지하는 믹스인
@Mixin(ClientPacketListener.class)
public class RiderTachoEnableMixin {

    @Inject(
        method = "handleSetEntityPassengersPacket",
        at = @At("TAIL")
    )
    private void dynrider$onSetPassengers(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        Entity subject = KartDetector.INSTANCE.getSubject(mc);
        if (subject == null || mc.level == null) return;

        int subjectId = subject.getId();
        int vehicleId = packet.getVehicle();

        boolean iAmPassenger = Arrays
            .stream(packet.getPassengers())
            .anyMatch(id -> id == subjectId);
        if (iAmPassenger) {
            RiderMountState.markMounted(vehicleId);
            KartDetector.detectKart(vehicleId);
            return;
        }

        if (RiderMountState.wasPassenger
            && RiderMountState.currentVehicleId != null
            && RiderMountState.currentVehicleId == vehicleId) {
            RiderMountState.reset();
            KartDetector.triggerDismounted();
        }
    }

    @Inject(
        method = "handleSetCamera(Lnet/minecraft/network/protocol/game/ClientboundSetCameraPacket;)V",
        at = @At("TAIL")
    )
    private void dynrider$onSetCamera(ClientboundSetCameraPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        Entity subject = KartDetector.INSTANCE.getSubject(mc);
        if (subject == null) return;

        Entity vehicle = subject.getVehicle();

        if (vehicle != null) {
            int vehicleId = vehicle.getId();
            RiderMountState.markMounted(vehicleId);
            KartDetector.detectKart(vehicleId);
            return;
        }

        if (RiderMountState.wasPassenger) {
            RiderMountState.reset();
            KartDetector.triggerDismounted();
        }

    }

    @Inject(method = "handleRemoveEntities", at = @At("TAIL"))
    private void dynrider$onRemoveEntities(
        ClientboundRemoveEntitiesPacket packet,
        CallbackInfo ci
    ) {
        Cod kart = KartDetector.getCurrentKart();
        if  (kart == null) return;
        int kartId =  kart.getId();

        for (int id : packet.getEntityIds()) {
            if (id == kartId) {
                RiderMountState.reset();
                KartDetector.triggerDismounted();
                return;
            }
        }
    }

}
