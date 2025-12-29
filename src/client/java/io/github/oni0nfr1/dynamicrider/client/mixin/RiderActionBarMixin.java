package io.github.oni0nfr1.dynamicrider.client.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression;
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartGaugeMeasure;
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartNitroCounter;
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartSpeedMeasure;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class RiderActionBarMixin {

    @Inject(
        method = "setActionBarText",
        at = @At("TAIL")
    )
    private void dynrider$readActionBar(
            @NotNull ClientboundSetActionBarTextPacket packet,
            CallbackInfo ci
    ) {
        Component actionBar = packet.text();
        String actionBarRaw = actionBar.getString();
        if (actionBarRaw.contains("km/h")) { // 카트 상태를 보여주는 액션바가 맞는지 확인
            KartGaugeMeasure.updateGauge(actionBar);
            KartSpeedMeasure.updateSpeed(actionBarRaw);
            KartNitroCounter.updateNitro(actionBarRaw);
        }
    }

    @WrapWithCondition(
        method = "setActionBarText(Lnet/minecraft/network/protocol/game/ClientboundSetActionBarTextPacket;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Gui;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V"
        )
    )
    private static boolean dynrider$blockActionBar(
        Gui instance,
        Component component,
        boolean bl
    ) {
        return !VanillaSuppression.getSuppressVanillaKartState();
    }

}
