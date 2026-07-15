package io.github.oni0nfr1.dynamicrider.client.hud.editor

import io.github.oni0nfr1.dynamicrider.client.hud.editor.gui.HudEditorLauncherScreen
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorSessionFactory
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneRepository
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screens.Screen

/** Config 경로와 repository를 조립하고 GUI에는 session factory만 전달하는 editor 진입점이다. */
object HudEditorEntrypoint {
    fun createLauncher(parentScreen: Screen): Screen {
        val customRoot = FabricLoader.getInstance().configDir.resolve("dynrider")
        val sessionFactory = HudEditorSessionFactory(HudSceneRepository(customRoot))
        return HudEditorLauncherScreen(parentScreen, sessionFactory)
    }
}
