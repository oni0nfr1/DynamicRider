package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient;
import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression;
import io.github.oni0nfr1.skid.client.api.kart.KartUtils;
import io.github.oni0nfr1.skid.client.api.kart.KartMountState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Unique
    private static final String RACE_TIMER_OBJECTIVE_NAME = "timerdisplay";

    @Inject(method = "renderOverlayMessage", at = @At("HEAD"), cancellable = true)
    private void onRenderOverlayMessage(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (DynamicRiderClient.getInstance().getCurrentScene() != null) ci.cancel();
    }

    @Inject(
            method = "displayScoreboardSidebar",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelScoreboardSidebar(
            GuiGraphics guiGraphics,
            Objective objective,
            CallbackInfo ci
    ) {
        if (
                objective.getName().equals(RACE_TIMER_OBJECTIVE_NAME)
                        && VanillaSuppression.getSuppressVanillaSidebarRanking()
        ) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderHotbarAndDecorations",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelHotbarAndDecorations(
            GuiGraphics guiGraphics,
            DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;
        KartMountState mountState = KartUtils.getMountState(player);

        boolean shouldShow = mountState instanceof KartMountState.None || !VanillaSuppression.getSuppressVanillaHotbar();
        if (!shouldShow) ci.cancel();
    }

    @Inject(
            method = "renderExperienceLevel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelExperienceLevel(
            GuiGraphics guiGraphics,
            DeltaTracker deltaTracker,
            CallbackInfo ci
    ) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;
        KartMountState mountState = KartUtils.getMountState(player);

        boolean shouldShow = mountState instanceof KartMountState.None || !VanillaSuppression.getSuppressVanillaHotbar();
        if (!shouldShow) ci.cancel();
    }
}
