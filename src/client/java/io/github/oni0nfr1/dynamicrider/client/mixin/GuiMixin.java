package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.config.DynRiderConfig;
import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression;
import io.github.oni0nfr1.skid.client.api.kart.Kart;
import io.github.oni0nfr1.skid.client.api.kart.KartRef;
import io.github.oni0nfr1.skid.client.api.kart.KartUtils;
import io.github.oni0nfr1.skid.client.api.kart.MountType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cod;
import net.minecraft.world.entity.player.Player;
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
        Player clientPlayer = Minecraft.getInstance().player;
        if (clientPlayer == null) return;
        Entity saddle = clientPlayer.getVehicle();
        if (!(saddle instanceof Cod)) return;
        KartRef kartRef = KartUtils.getKart((Cod) saddle);
        if (kartRef == null) return;
        Kart kart = kartRef.getHandle();
        if (kart == null) return;
        if (kart.getAlive() && DynRiderConfig.INSTANCE.isModEnabled()) ci.cancel(); // 카트 탑승 시에 나오는 액션바 차단
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
