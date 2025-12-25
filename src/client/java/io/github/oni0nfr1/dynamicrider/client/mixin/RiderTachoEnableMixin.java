package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.rider.KartDetector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

// 타코미터 HUD를 생성해야 할지를 감지하는 믹스인
@Mixin(ClientPacketListener.class)
public class RiderTachoEnableMixin {

    @Unique
    private static boolean dynrider$wasPassenger = false;
    @Unique
    private static Integer dynrider$currentVehicleId = null;

    @Inject(
        method = "handleSetEntityPassengersPacket",
        at = @At("TAIL")
    )
    private void dynrider$onSetPassengers(ClientboundSetPassengersPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int playerId = mc.player.getId();
        int vehicleId = packet.getVehicle();

        boolean iAmPassenger = Arrays
            .stream(packet.getPassengers())
            .anyMatch(id -> id == playerId);
        if (!iAmPassenger) {
            if (dynrider$wasPassenger
                && dynrider$currentVehicleId != null
                && dynrider$currentVehicleId == vehicleId) {
                dynrider$wasPassenger = false;
                dynrider$currentVehicleId = null;
                KartDetector.INSTANCE.onKartDismount();
            }
            return;
        }

        dynrider$wasPassenger = true;
        dynrider$currentVehicleId = vehicleId;

        KartDetector.detectKart(vehicleId);
    }


}
