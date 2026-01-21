package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.event.RiderLapFinishCallback;
import io.github.oni0nfr1.dynamicrider.client.rider.chat.LapMessage;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class RiderChatMsgMixin {

    @Inject(method = "handleSystemChat", at = @At("TAIL"))
    private void onSystemChat(
            ClientboundSystemChatPacket packet,
            CallbackInfo ci
    ) {
        Component component = packet.content();
        LapMessage msg = LapMessage.parseLapMessage(component);
        if (msg == null) return;

        RiderLapFinishCallback.EVENT.invoker().handle(msg);
    }

}
