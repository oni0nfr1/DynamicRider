package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.event.RiderExpUpdateCallback;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class RiderExperienceMixin {
    @Inject(method = "handleSetExperience", at = @At("TAIL"))
    private void onSetExperience(ClientboundSetExperiencePacket packet, CallbackInfo ci) {
        float progress = packet.getExperienceProgress();
        int level = packet.getExperienceLevel();
        int total = packet.getTotalExperience();
        RiderExpUpdateCallback.EVENT.invoker().handle(progress, level, total);
    }
}
