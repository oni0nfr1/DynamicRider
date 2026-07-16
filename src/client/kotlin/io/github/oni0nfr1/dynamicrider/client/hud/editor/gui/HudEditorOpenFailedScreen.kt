package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

/** 초기 장면을 열지 못했을 때 재시도하거나 설정 화면으로 돌아갈 수 있게 한다. */
class HudEditorOpenFailedScreen(
    private val parentScreen: Screen,
    private val failure: Component,
    private val retry: () -> Screen,
) : Screen(Component.translatable("dynamicrider.hud.editor.open_failed.title")) {
    override fun init() {
        addRenderableWidget(
            Button.builder(Component.translatable("dynamicrider.hud.editor.retry")) {
                Minecraft.getInstance().setScreen(retry())
            }.bounds(width / 2 - 104, height / 2 + 24, 100, 20).build()
        )
        addRenderableWidget(
            Button.builder(Component.translatable("gui.back")) { onClose() }
                .bounds(width / 2 + 4, height / 2 + 24, 100, 20)
                .build()
        )
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.drawCenteredString(font, title, width / 2, height / 2 - 36, 0xFFFFFFFF.toInt())
        guiGraphics.drawWordWrap(font, failure, width / 2 - 120, height / 2 - 12, 240, 0xFFFF7777.toInt())
    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parentScreen)
    }
}
