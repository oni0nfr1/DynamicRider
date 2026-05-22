package io.github.oni0nfr1.dynamicrider.client.hud.scene.custom

import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import java.nio.file.Path

object HudScenePaths {
    fun customHudScenePath(
        root: Path,
        mode: HudSceneMode,
        engineType: KartEngine.Type,
    ): Path {
        val modeDirectoryName = when (mode) {
            HudSceneMode.RIDE -> "ride"
            HudSceneMode.SPECTATE -> "spectate"
        }
        val engineFileName = "${engineType.name.lowercase()}.json"
        return root.resolve("hud").resolve(modeDirectoryName).resolve(engineFileName)
    }
}
