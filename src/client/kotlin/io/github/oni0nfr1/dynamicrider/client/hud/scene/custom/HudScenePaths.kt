package io.github.oni0nfr1.dynamicrider.client.hud.scene.custom

import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import java.nio.file.Path
import net.minecraft.resources.ResourceLocation
import io.github.oni0nfr1.dynamicrider.client.ResourceStore

object HudScenePaths {
    private fun modeDirectoryName(mode: HudSceneMode): String = when (mode) {
        HudSceneMode.RIDE -> "ride"
        HudSceneMode.SPECTATE -> "spectate"
    }

    fun customHudScenePath(
        root: Path,
        mode: HudSceneMode,
        engineType: KartEngine.Type,
    ): Path {
        val modeDirectoryName = modeDirectoryName(mode)
        val engineFileName = "${engineType.name.lowercase()}.json"
        return root.resolve("hud").resolve(modeDirectoryName).resolve(engineFileName)
    }

    fun resourceHudSceneId(mode: HudSceneMode, engineType: KartEngine.Type): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath(
            ResourceStore.MOD_ID,
            "${modeDirectoryName(mode)}/${engineType.name.lowercase()}",
        )

    fun defaultResourceHudSceneId(mode: HudSceneMode): ResourceLocation =
        ResourceLocation.fromNamespaceAndPath(
            ResourceStore.MOD_ID,
            "${modeDirectoryName(mode)}/default",
        )

    fun resourceDisplayPath(mode: HudSceneMode, engineType: KartEngine.Type): Path =
        Path.of(
            "assets",
            ResourceStore.MOD_ID,
            "hud",
            modeDirectoryName(mode),
            "${engineType.name.lowercase()}.json",
        )

    fun defaultResourceDisplayPath(mode: HudSceneMode): Path =
        Path.of(
            "assets",
            ResourceStore.MOD_ID,
            "hud",
            modeDirectoryName(mode),
            "default.json",
        )
}
