package io.github.oni0nfr1.dynamicrider.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.UUID;


@Mixin(BossHealthOverlay.class)
public abstract class BossbarSuppressionMixin {
    @Shadow
    @Final
    Map<UUID, LerpingBossEvent> events;

    @Unique private static final String TEAM_NITRO_TITLE = "TEAM NITRO";

    @ModifyExpressionValue(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;"
            ),
            require = 1
    )
    private java.util.Iterator<LerpingBossEvent> dynrider$filterBossBarsIterator(
            java.util.Iterator<LerpingBossEvent> originalIterator
    ) {
        java.util.ArrayList<LerpingBossEvent> filtered = new java.util.ArrayList<>();

        for (LerpingBossEvent e : this.events.values()) {
            if (!shouldHideBossBar(e)) filtered.add(e);
        }

        return filtered.iterator();
    }

    @Unique
    private static boolean shouldHideBossBar(LerpingBossEvent bossEvent) {
        String titlePlain = bossEvent.getName().getString();
        if (
                titlePlain.equals(TEAM_NITRO_TITLE)
                && VanillaSuppression.getSuppressTeamBoosterBossbar()
        ) return true;

        return false;
    }
}
