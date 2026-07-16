package io.github.oni0nfr1.dynamicrider.client.hud.editor

import io.github.oni0nfr1.dynamicrider.client.hud.editor.gui.HudEditorOpenFailedScreen
import io.github.oni0nfr1.dynamicrider.client.hud.editor.gui.HudEditorPreviewViewport
import io.github.oni0nfr1.dynamicrider.client.hud.editor.gui.HudEditorSceneSelection
import io.github.oni0nfr1.dynamicrider.client.hud.editor.gui.HudEditorScreen
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorSessionFactory
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorSessionOpenResult
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneRepository
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

/** Config 경로와 repository를 조립하고 GUI에는 session factory만 전달하는 editor 진입점이다. */
object HudEditorEntrypoint {
    fun createEditor(parentScreen: Screen): Screen {
        val customRoot = FabricLoader.getInstance().configDir.resolve("dynrider")
        val sessionFactory = HudEditorSessionFactory(HudSceneRepository(customRoot))
        return openEditor(parentScreen, sessionFactory)
    }

    private fun openEditor(parentScreen: Screen, sessionFactory: HudEditorSessionFactory): Screen {
        val viewport = HudEditorPreviewViewport()
        return when (
            val result = sessionFactory.open(
                HudEditorSceneSelection.mode,
                HudEditorSceneSelection.stateType,
                viewport,
            )
        ) {
            is HudEditorSessionOpenResult.Opened -> HudEditorScreen(
                parentScreen,
                sessionFactory,
                result.session,
                viewport,
            )
            is HudEditorSessionOpenResult.ResolveFailed -> HudEditorOpenFailedScreen(
                parentScreen,
                Component.translatable(
                    "dynamicrider.hud.editor.open_failed",
                    result.errors.size,
                ),
            ) { openEditor(parentScreen, sessionFactory) }
            is HudEditorSessionOpenResult.PreviewCreationFailed -> HudEditorOpenFailedScreen(
                parentScreen,
                Component.translatable("dynamicrider.hud.editor.preview_failed"),
            ) { openEditor(parentScreen, sessionFactory) }
        }
    }
}
