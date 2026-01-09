package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.event.RiderPlayerInfoRemoveCallback;
import io.github.oni0nfr1.dynamicrider.client.event.RiderPlayerInfoUpdateCallback;
import io.github.oni0nfr1.dynamicrider.client.event.RiderSetSidebarContentCallback;
import io.github.oni0nfr1.dynamicrider.client.event.RiderSetSidebarTitleCallback;
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.SidebarSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class RiderScoreboardMixin {

    @Inject(method = "handleSetDisplayObjective", at = @At("TAIL"))
    private void dynrider$onDisplayObjective(ClientboundSetDisplayObjectivePacket packet, CallbackInfo ci) {
        @NotNull Minecraft client = Minecraft.getInstance();
        @Nullable SidebarSnapshot snapshot = SidebarSnapshot.fromMcClient(client);
        if (snapshot == null) return;

        RiderSetSidebarTitleCallback.EVENT.invoker()
            .handle(snapshot.getTitle(), snapshot.getTitle().getString());
        RiderSetSidebarContentCallback.EVENT.invoker().handle(snapshot);
    }

    @Inject(method = "handleAddObjective", at = @At("TAIL"))
    private void dynrider$onObjective(ClientboundSetObjectivePacket packet, CallbackInfo ci) {
        @NotNull Minecraft client = Minecraft.getInstance();
        @Nullable SidebarSnapshot snapshot = SidebarSnapshot.fromMcClient(client);
        if (snapshot == null) return;

        RiderSetSidebarTitleCallback.EVENT.invoker()
            .handle(snapshot.getTitle(), snapshot.getTitle().getString());
        RiderSetSidebarContentCallback.EVENT.invoker().handle(snapshot);
    }

    @Inject(method = "handleSetScore", at = @At("TAIL"))
    private void dynrider$onSetScore(ClientboundSetScorePacket packet, CallbackInfo ci) {
        @NotNull Minecraft client = Minecraft.getInstance();
        @Nullable SidebarSnapshot snapshot = SidebarSnapshot.fromMcClient(client);
        if (snapshot == null) return;

        RiderSetSidebarContentCallback.EVENT.invoker().handle(snapshot);
    }

    @Inject(method = "handleResetScore", at = @At("TAIL"))
    private void dynrider$onResetScore(ClientboundResetScorePacket packet, CallbackInfo ci) {
        @NotNull Minecraft client = Minecraft.getInstance();
        @Nullable SidebarSnapshot snapshot = SidebarSnapshot.fromMcClient(client);
        if (snapshot == null) return;

        RiderSetSidebarContentCallback.EVENT.invoker().handle(snapshot);
    }

    @Inject(method = "handleSetPlayerTeamPacket", at = @At("TAIL"))
    private void dynrider$onTeam(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        @NotNull Minecraft client = Minecraft.getInstance();
        @Nullable SidebarSnapshot snapshot = SidebarSnapshot.fromMcClient(client);
        if (snapshot == null) return;

        RiderSetSidebarContentCallback.EVENT.invoker().handle(snapshot);
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At("TAIL"))
    private void dynrider$onPlayerRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
        RiderPlayerInfoRemoveCallback.EVENT.invoker().handle(packet.profileIds());
    }

    @Inject(method = "handlePlayerInfoUpdate", at = @At("TAIL"))
    private void dynrider$onPlayerUpdate(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
        RiderPlayerInfoUpdateCallback.EVENT.invoker().handle(packet);
    }
}
