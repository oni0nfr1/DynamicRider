package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorSessionFactory
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorSessionOpenResult
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.CycleButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

/** 편집할 HUD 모드와 preview 카트 상태 타입을 선택해 editor session을 여는 화면이다. */
class HudEditorLauncherScreen(
    private val parentScreen: Screen,
    private val sessionFactory: HudEditorSessionFactory,
) : Screen(Component.translatable("dynamicrider.hud.editor.launcher.title")) {
    private var mode: HudSceneMode = HudSceneMode.RIDE
    private var stateType: KartStateType<out KartState> = KartStateTypes.JIU
    private var status: Component? = null

    override fun init() {
        val centerX = width / 2
        val rowWidth = 240
        val firstRowY = height / 2 - 50

        addRenderableWidget(
            CycleButton.builder<HudSceneMode> { Component.literal(it.name) }
                .withValues(HudSceneMode.entries)
                .withInitialValue(mode)
                .create(
                    centerX - rowWidth / 2,
                    firstRowY,
                    rowWidth,
                    20,
                    Component.translatable("dynamicrider.hud.editor.mode"),
                ) { _, value -> mode = value }
        )
        addRenderableWidget(
            CycleButton.builder<KartStateType<out KartState>> { Component.literal(it.id.uppercase()) }
                .withValues(KartStateTypes.entries)
                .withInitialValue(stateType)
                .create(
                    centerX - rowWidth / 2,
                    firstRowY + 24,
                    rowWidth,
                    20,
                    Component.translatable("dynamicrider.hud.editor.state_type"),
                ) { _, value -> stateType = value }
        )
        addRenderableWidget(
            Button.builder(Component.translatable("dynamicrider.hud.editor.open")) { openEditor() }
                .bounds(centerX - 120, firstRowY + 56, 116, 20)
                .build()
        )
        addRenderableWidget(
            Button.builder(Component.translatable("gui.cancel")) { onClose() }
                .bounds(centerX + 4, firstRowY + 56, 116, 20)
                .build()
        )
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        super.render(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.drawCenteredString(font, title, width / 2, height / 2 - 82, 0xFFFFFF)
        status?.let {
            guiGraphics.drawCenteredString(font, it, width / 2, height / 2 + 38, 0xFFFF6666.toInt())
        }
    }

    override fun onClose() {
        Minecraft.getInstance().setScreen(parentScreen)
    }

    private fun openEditor() {
        val viewport = HudEditorPreviewViewport()
        when (val result = sessionFactory.open(mode, stateType, viewport)) {
            is HudEditorSessionOpenResult.Opened -> Minecraft.getInstance().setScreen(
                HudEditorScreen(parentScreen, result.session, viewport)
            )
            is HudEditorSessionOpenResult.ResolveFailed -> {
                status = Component.translatable("dynamicrider.hud.editor.open_failed", result.errors.size)
            }
            is HudEditorSessionOpenResult.PreviewCreationFailed -> {
                status = Component.translatable("dynamicrider.hud.editor.preview_failed")
            }
        }
    }
}
