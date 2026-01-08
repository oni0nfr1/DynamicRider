package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.SidebarProvider;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class RiderScoreboardMixin {

    @Inject(method = "handleSetDisplayObjective", at = @At("TAIL"))
    private void dynrider$onDisplayObjective(ClientboundSetDisplayObjectivePacket packet, CallbackInfo ci) {
        SidebarProvider.invalidate();
    }

    @Inject(method = "handleAddObjective", at = @At("TAIL"))
    private void dynrider$onObjective(ClientboundSetObjectivePacket packet, CallbackInfo ci) {
        SidebarProvider.invalidate();
    }

    @Inject(method = "handleSetScore", at = @At("TAIL"))
    private void dynrider$onSetScore(ClientboundSetScorePacket packet, CallbackInfo ci) {
        SidebarProvider.invalidate();
    }

    @Inject(method = "handleResetScore", at = @At("TAIL"))
    private void dynrider$onResetScore(ClientboundResetScorePacket packet, CallbackInfo ci) {
        SidebarProvider.invalidate();
    }

    @Inject(method = "handleSetPlayerTeamPacket", at = @At("TAIL"))
    private void dynrider$onTeam(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        SidebarProvider.invalidate();
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At("TAIL"))
    private void dynrider$onPlayerRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
        SidebarProvider.onPlayerInfoRemove(packet.profileIds());
    }

    @Inject(method = "handlePlayerInfoUpdate", at = @At("TAIL"))
    private void dynrider$onPlayerUpdate(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
        SidebarProvider.onPlayerInfoUpdate(packet);
    }
}
