package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.event.RiderSpectateCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class RiderSpectateMixin {
    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Nullable public Entity cameraEntity;

    @Inject(method = "setCameraEntity", at = @At("HEAD"))
    private void dynrider$hookSetCameraEntity(@Nullable Entity newCamera, CallbackInfo ci) {
        @Nullable LocalPlayer player = this.player;
        if (player == null || newCamera == null) return;
        if (!player.isSpectator()) return;

        Entity prev = this.cameraEntity;
        if (prev == newCamera) return;

        RiderSpectateCallback.EVENT.invoker().handle(player, newCamera);
    }
}
