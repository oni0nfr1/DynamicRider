package io.github.oni0nfr1.dynamicrider.client.mixin;

import io.github.oni0nfr1.dynamicrider.client.config.gui.DynRiderConfigMain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void dynamicrider$addButton(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();

        this.addRenderableWidget(
                Button.builder(
                        Component.literal("다이나믹 라이더 설정"),
                        (btn) -> client.setScreen(new DynRiderConfigMain(this))
                ).bounds(10, 60, 100, 20).build()
        );
    }
}
