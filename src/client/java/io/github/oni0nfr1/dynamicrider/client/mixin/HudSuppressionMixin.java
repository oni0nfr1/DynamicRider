package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression;
import io.github.oni0nfr1.skid.client.api.kart.KartUtils;
import io.github.oni0nfr1.skid.client.api.kart.MountType;
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
public abstract class HudSuppressionMixin {

    @Unique
    private static final String RACE_TIMER_OBJECTIVE_NAME = "timerdisplay";

//    현재 이 믹스인은 SkidMC와 충돌 가능성이 있으며, 곧 제거될 예정이며 더이상 메인 모드 흐름에 포함되지 않으므로 주석 처리되었습니다.
//    @Inject(
//            method = "setOverlayMessage",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void cancelActionBarMsg(
//            Component component,
//            boolean bl,
//            CallbackInfo ci
//    ) {
//        String raw = component.getString();
//        if (raw.contains("km/h")) {
//            HandleResult result = RiderActionBarCallback.EVENT.invoker().handle(component, raw);
//
//            boolean shouldCallOriginal = (result != HandleResult.FAILURE) &&
//                    !VanillaSuppression.getSuppressVanillaKartState();
//            if (!shouldCallOriginal) {
//                ci.cancel();
//            }
//        }
//    }

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
        MountType mountStatus = KartUtils.getMountStatus(player);

        boolean shouldShow = mountStatus instanceof MountType.Dismounted || !VanillaSuppression.getSuppressVanillaHotbar();
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
        MountType mountStatus = KartUtils.getMountStatus(player);

        boolean shouldShow = mountStatus instanceof MountType.Dismounted || !VanillaSuppression.getSuppressVanillaHotbar();
        if (!shouldShow) ci.cancel();
    }
}
