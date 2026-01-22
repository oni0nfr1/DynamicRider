package io.github.oni0nfr1.dynamicrider.client.hud.graphics

import io.github.oni0nfr1.dynamicrider.client.config.DynRiderConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

/*
    렌더 관련 함수들이 들어갑니다.
    좀더 다양한 디자인 구현을 위해 커스텀 draw call이 추가될 수도 있습니다.
*/

fun GuiGraphics.textWithDynriderFont(
    x: Int,
    y: Int,
    argbColor: Int,
    text: String,
    shadow: Boolean = false
) {
    val client = Minecraft.getInstance()
    val font = DynRiderConfig.hudFont.style
    val component = Component.literal(text).setStyle(font)
    this.drawString(client.font, component, x, y, argbColor, shadow)
}