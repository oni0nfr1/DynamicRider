package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class HideSidebarMixin {

    @Unique
    private static final String RACE_TIMER_OBJECTIVE_NAME = "timerdisplay";

    @Inject(
            method = "displayScoreboardSidebar",
            at = @At("HEAD"),
            cancellable = true
    )
    private void dynamicRider$cancelScoreboardSidebar(
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
}
