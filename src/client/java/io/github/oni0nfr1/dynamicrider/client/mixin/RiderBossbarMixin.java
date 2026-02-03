package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.event.bossbar.RiderBossbarAddCallback;
import io.github.oni0nfr1.dynamicrider.client.event.bossbar.RiderBossbarProgressCallback;
import io.github.oni0nfr1.dynamicrider.client.event.bossbar.RiderBossbarRemoveCallback;
import io.github.oni0nfr1.dynamicrider.client.event.bossbar.RiderBossbarRenameCallback;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ClientPacketListener.class)
public abstract class RiderBossbarMixin {
    @Inject(
            method = "handleBossUpdate",
            at = @At("TAIL")
    )
    private void onBossbarUpdate(
            ClientboundBossEventPacket packet,
            CallbackInfo ci
    ) {
        packet.dispatch(new ClientboundBossEventPacket.Handler() {
            @Override
            public void add(
                    UUID uuid,
                    Component name,
                    float progress,
                    BossEvent.BossBarColor color,
                    BossEvent.BossBarOverlay overlay,
                    boolean _bl, boolean _bl2, boolean _bl3
            ) {
                RiderBossbarAddCallback.EVENT.invoker().handle(uuid, name, progress);
            }

            @Override
            public void remove(UUID uuid) {
                RiderBossbarRemoveCallback.EVENT.invoker().handle(uuid);
            }

            @Override
            public void updateProgress(UUID uuid, float progress) {
                RiderBossbarProgressCallback.EVENT.invoker().handle(uuid, progress);
            }

            @Override
            public void updateName(UUID uuid, Component name) {
                RiderBossbarRenameCallback.EVENT.invoker().handle(uuid, name);
            }
        });
    }
}
