package io.github.oni0nfr1.dynamicrider.client.hud.scene.config

import io.github.oni0nfr1.dynamicrider.client.config.DynRiderConfig
import io.github.oni0nfr1.dynamicrider.client.hud.scene.layouts.HudScene
import io.github.oni0nfr1.dynamicrider.client.util.warnLog
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

object HudSceneResolver {

    fun createScene(
        engine: KartEngine.Type,
        mode: HudSceneMode,
        kart: KartRef.Specific<NitroEngine>,
    ): HudScene<NitroEngine> {
        val key = HudSceneKey(engine, mode)

        if (DynRiderConfig.currentData.uiMode != HudUiMode.CUSTOM) {
            return BuiltinHudScenes.create(key, kart)
        }

        val loadResult = loadConfiguredOrDefault(key)

        return when (loadResult) {
            is LoadCustomHudResult.Success -> {
                if (loadResult.scene.key != key) {
                    warnLog(
                        "Using builtin HUD scene because custom scene '${loadResult.scene.fileName}' targets " +
                                "${loadResult.scene.key.engineKey}/${loadResult.scene.key.mode.name.lowercase()}, not ${key.engineKey}/${key.mode.name.lowercase()}"
                    )
                    BuiltinHudScenes.create(key, kart)
                } else {
                    loadResult.scene.spec.toHudScene(kart)
                }
            }

            is LoadCustomHudResult.Failure -> {
                warnLog("Using builtin HUD scene because custom scene '${loadResult.fileName}' failed: ${loadResult.reason}")
                BuiltinHudScenes.create(key, kart)
            }
        }
    }

    private fun loadConfiguredOrDefault(key: HudSceneKey): LoadCustomHudResult {
        val configuredName = DynRiderConfig.customSceneName(key.engineKey, key.mode)
        if (configuredName != null) {
            val configuredResult = CustomHudSceneLoader.load(configuredName)
            if (configuredResult is LoadCustomHudResult.Success) return configuredResult
            if (configuredResult is LoadCustomHudResult.Failure &&
                configuredResult.kind != LoadFailureKind.NOT_FOUND
            ) {
                return configuredResult
            }

            warnLog("Removing stale custom HUD scene mapping ${key.engineKey}/${key.mode.name.lowercase()} -> $configuredName")
            DynRiderConfig.removeCustomSceneName(key.engineKey, key.mode)
        }

        return CustomHudSceneLoader.ensureDefaultFor(key).also { result ->
            if (result is LoadCustomHudResult.Success) {
                DynRiderConfig.setCustomSceneName(key.engineKey, key.mode, result.scene.fileName)
            }
        }
    }

    fun sceneName(engine: KartEngine.Type, mode: HudSceneMode): String {
        val key = HudSceneKey(engine, mode)

        if (DynRiderConfig.currentData.uiMode != HudUiMode.CUSTOM) {
            return BuiltinHudScenes.displayName(key)
        }

        val configuredName = DynRiderConfig.customSceneName(key.engineKey, mode)
            ?: defaultCustomSceneName(key)

        return when (val result = CustomHudSceneLoader.load(configuredName)) {
            is LoadCustomHudResult.Success -> "custom/${result.scene.displayName}"
            is LoadCustomHudResult.Failure -> "${BuiltinHudScenes.displayName(key)} (custom '$configuredName' unavailable)"
        }
    }
}
