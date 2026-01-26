package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient;
import io.github.oni0nfr1.dynamicrider.client.event.RiderActionBarCallback;
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult;
import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression;
import io.github.oni0nfr1.dynamicrider.client.rider.mount.KartMountDetector;
import io.github.oni0nfr1.dynamicrider.client.rider.mount.MountType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class HudSuppressionMixin {

    @Unique
    private static final String RACE_TIMER_OBJECTIVE_NAME = "timerdisplay";

    @Inject(
            method = "setOverlayMessage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelActionBarMsg(
            Component component,
            boolean bl,
            CallbackInfo ci
    ) {
        String raw = component.getString();
        if (raw.contains("km/h")) {
            HandleResult result = RiderActionBarCallback.EVENT.invoker().handle(component, raw);

            boolean shouldCallOriginal = (result != HandleResult.FAILURE) &&
                    !VanillaSuppression.getSuppressVanillaKartState();
            if (!shouldCallOriginal) {
                ci.cancel();
            }
        }
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
        DynamicRiderClient dynrider = DynamicRiderClient.getInstance();
        KartMountDetector mountDetector = dynrider.getMountDetector();
        MountType mounted = mountDetector.getPlayerMountStatus().invoke();

        boolean shouldShow = mounted == MountType.NOT_MOUNTED || !VanillaSuppression.getSuppressVanillaHotbar();
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
        DynamicRiderClient dynrider = DynamicRiderClient.getInstance();
        KartMountDetector mountDetector = dynrider.getMountDetector();
        MountType mounted = mountDetector.getPlayerMountStatus().invoke();

        boolean shouldShow = mounted == MountType.NOT_MOUNTED || !VanillaSuppression.getSuppressVanillaHotbar();
        if (!shouldShow) ci.cancel();
    }
}
