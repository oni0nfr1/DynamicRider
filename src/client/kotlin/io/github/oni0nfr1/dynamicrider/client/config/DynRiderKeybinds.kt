package io.github.oni0nfr1.dynamicrider.client.config

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

object DynRiderKeybinds {

    private val toggleHudKeyBinding: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.dynrider.toggle_hud",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "key.category.dynrider"
        )
    )

    /** HUD 토글 키의 표시 이름을 반환하며, 미할당 상태라면 `null`이다. */
    val toggleHudKeyName: Component?
        get() = if (toggleHudKeyBinding.isUnbound) null else toggleHudKeyBinding.translatedKeyMessage

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.screen != null) return@register

            if (toggleHudKeyBinding.consumeClick()) {
                DynRiderConfig.hudVisible = !DynRiderConfig.hudVisible

                val key = if (DynRiderConfig.hudVisible) "dynrider.hud.enabled"
                          else "dynrider.hud.disabled"
                client.gui.setOverlayMessage(Component.translatable(key), false)

                while (toggleHudKeyBinding.consumeClick()) { /* flush */ }
            }
        }
    }

}
