package io.github.oni0nfr1.dynamicrider.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.oni0nfr1.dynamicrider.client.event.RiderActionBarCallback;
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult;
import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPacketListener.class)
public abstract class RiderActionBarMixin {

    @WrapOperation(
        method = "setActionBarText(Lnet/minecraft/network/protocol/game/ClientboundSetActionBarTextPacket;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V"
        )
    )
    private void dynrider$onActionBar(
            Gui instance,
            Component component,
            boolean bl,
            Operation<Void> original
    ) {
        String raw = component.getString();
        if (raw.contains("km/h")) {
            ClientPacketListener self = (ClientPacketListener) (Object) this;
            HandleResult result = RiderActionBarCallback.EVENT.invoker().handle(self, component, raw);

            boolean shouldCallOriginal = (result != HandleResult.FAILURE) &&
                    !VanillaSuppression.getSuppressVanillaKartState();
            if (shouldCallOriginal) {
                original.call(instance, component, bl);
            }
        }
    }
}
