package io.github.oni0nfr1.dynamicrider.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class HideCodMixin {
    @Inject(
        method = "renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void mcrider$hideInvisibleCodWhenSpectating(
        Entity entity,
        double camX,
        double camY,
        double camZ,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffers,
        CallbackInfo ci
    ) {
        if (entity.getType() != EntityType.COD) return;

        boolean invisible = entity.isInvisible();

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player != null) {
            invisible = invisible || entity.isInvisibleTo(player);
        }

        if (invisible) {
            ci.cancel();
        }
    }
}
