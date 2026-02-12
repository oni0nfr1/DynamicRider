package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.event.attribute.RiderAttrPacketCallback;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class RiderAttrPacketMixin {

    @Inject(
            method = "handleUpdateAttributes",
            at = @At("TAIL")
    )
    private void onUpdateAttributes(
            ClientboundUpdateAttributesPacket packet,
            CallbackInfo ci
    ) {
        RiderAttrPacketCallback.invokeEvent(packet);
    }
}
