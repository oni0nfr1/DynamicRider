package io.github.oni0nfr1.dynamicrider.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.oni0nfr1.dynamicrider.client.event.*;
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.SidebarSnapshot;
import io.github.oni0nfr1.dynamicrider.client.event.RiderRaceEndCallback.RaceEndReason;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class RiderScoreboardMixin {

    @Final
    @Shadow
    private Scoreboard scoreboard;

    @Unique
    private static final String RACE_TIMER_OBJECTIVE_NAME = "timerdisplay";

    @WrapOperation(
            method = "handleSetDisplayObjective",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/scores/Scoreboard;setDisplayObjective(Lnet/minecraft/world/scores/DisplaySlot;Lnet/minecraft/world/scores/Objective;)V"
            )
    )
    private void wrapSetDisplayObjective(
            Scoreboard scoreboard,
            DisplaySlot displaySlot,
            @Nullable Objective newObjective,
            Operation<Void> original,
            ClientboundSetDisplayObjectivePacket packet
    ) {
        if (displaySlot != DisplaySlot.SIDEBAR) {
            original.call(scoreboard, displaySlot, newObjective);
            return;
        }

        @Nullable Objective previousSidebarObjective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        String previousObjectiveName = previousSidebarObjective != null ? previousSidebarObjective.getName() : "";
        String newObjectiveName = newObjective != null ? newObjective.getName() : "";

        boolean wasTimerDisplay = RACE_TIMER_OBJECTIVE_NAME.equals(previousObjectiveName);
        boolean isTimerDisplay = RACE_TIMER_OBJECTIVE_NAME.equals(newObjectiveName);

        if (!wasTimerDisplay && isTimerDisplay) {
            RiderRaceStartCallback.EVENT.invoker().handle();
        } else if (wasTimerDisplay && !isTimerDisplay) {
            RiderRaceEndCallback.EVENT.invoker().handle(RaceEndReason.FINISH);
        }

        original.call(scoreboard, displaySlot, newObjective);
    }

    @WrapOperation(
            method = "handleAddObjective",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/scores/Scoreboard;removeObjective(Lnet/minecraft/world/scores/Objective;)V"
            )
    )
    private void dynamicrider$beforeRemoveObjective(
            Scoreboard scoreboard,
            Objective removedObjective,
            Operation<Void> original
    ) {
        Objective sidebarObjectiveBeforeRemoval = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);

        boolean wasDisplayedInSidebar =
            sidebarObjectiveBeforeRemoval == removedObjective || (
                sidebarObjectiveBeforeRemoval != null
                && sidebarObjectiveBeforeRemoval.getName().equals(removedObjective.getName())
            );

        if (wasDisplayedInSidebar && RACE_TIMER_OBJECTIVE_NAME.equals(removedObjective.getName())) {
            RiderRaceEndCallback.EVENT.invoker().handle(RaceEndReason.FINISH);
        }

        original.call(scoreboard, removedObjective);
    }


    @Inject(method = "handleSetDisplayObjective", at = @At("TAIL"))
    private void onDisplayObjectiveTail(ClientboundSetDisplayObjectivePacket packet, CallbackInfo ci) {
        @NotNull Minecraft client = Minecraft.getInstance();
        @Nullable SidebarSnapshot snapshot = SidebarSnapshot.fromMcClient(client);
        if (snapshot == null || snapshot.getObjective() == null) return;

        if (snapshot.getObjective().getName().equals(RACE_TIMER_OBJECTIVE_NAME)) {
            RiderTimerUpdateCallback.EVENT.invoker()
                    .handle(snapshot.getTitle(), snapshot.getTitle().getString());
            RiderRankingUpdateCallback.EVENT.invoker().handle(snapshot);
        }
    }

    @Inject(method = "handleSetScore", at = @At("TAIL"))
    private void onSetScore(ClientboundSetScorePacket packet, CallbackInfo ci) {
        @NotNull Minecraft client = Minecraft.getInstance();
        if (scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR) == null) return;

        if (packet.owner().equals("timertext")) {
            if (packet.display().isPresent()) {
                RiderTimerUpdateCallback.EVENT.invoker()
                        .handle(packet.display().get(), packet.display().get().getString());
            }
        } else if (!packet.owner().equals("timertext2")) {
            @Nullable SidebarSnapshot snapshot = SidebarSnapshot.fromMcClient(client);
            if (snapshot != null) RiderRankingUpdateCallback.EVENT.invoker().handle(snapshot);
        }
    }

    @Inject(method = "handleSetPlayerTeamPacket", at = @At("TAIL"))
    private void onTeam(ClientboundSetPlayerTeamPacket packet, CallbackInfo ci) {
        @NotNull Minecraft client = Minecraft.getInstance();
        @Nullable SidebarSnapshot snapshot = SidebarSnapshot.fromMcClient(client);
        if (snapshot == null) return;

        RiderRankingUpdateCallback.EVENT.invoker().handle(snapshot);
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At("TAIL"))
    private void onPlayerRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
        RiderPlayerInfoRemoveCallback.EVENT.invoker().handle(packet.profileIds());
    }

    @Inject(method = "handlePlayerInfoUpdate", at = @At("TAIL"))
    private void onPlayerUpdate(ClientboundPlayerInfoUpdatePacket packet, CallbackInfo ci) {
        RiderPlayerInfoUpdateCallback.EVENT.invoker().handle(packet);
    }
}
