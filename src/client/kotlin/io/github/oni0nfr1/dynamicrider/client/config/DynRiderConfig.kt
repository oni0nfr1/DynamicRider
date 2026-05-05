package io.github.oni0nfr1.dynamicrider.client.config

import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.CustomSceneSelection
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HudUiMode
import io.github.oni0nfr1.dynamicrider.client.util.warnLog
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.io.path.readText

object DynRiderConfig {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val configFilePath: Path =
        FabricLoader.getInstance().configDir.resolve("dynrider.json")

    var currentData: DynRiderConfigData = DynRiderConfigData()
        private set

    //////////////////// 실제 반영되는 설정값들 ////////////////////////

    var isModEnabled: Boolean = true
        set(value) {
            VanillaSuppression.suppressionEnabled = value
            field = value
        }
    var hudVisible: Boolean = true
    var hudFont: FontStyle = FontStyle.VANILLA

    /////////////////////////////////////////////////////////////////

    fun load() {
        currentData = try {
            if (!configFilePath.exists()) {
                val defaultConfig = DynRiderConfigData()
                save(defaultConfig)
                defaultConfig
            } else {
                val rawJson = configFilePath.readText()
                json.decodeFromString<DynRiderConfigData>(rawJson)
            }
        } catch (exception: Exception) {
            warnLog("Exception thrown while reading Config file. Applying default config...")
            exception.printStackTrace()
            val fallbackConfig = DynRiderConfigData()
            save(fallbackConfig)
            fallbackConfig
        }
    }

    fun save(data: DynRiderConfigData) {
        currentData = data

        try {
            Files.createDirectories(configFilePath.parent)

            val tempPath = configFilePath.resolveSibling("${configFilePath.fileName}.tmp")
            val content = json.encodeToString(data)

            tempPath.writeText(content)
            Files.move(
                tempPath,
                configFilePath,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    fun apply(data: DynRiderConfigData) {
        isModEnabled = data.isModEnabled
        hudFont = runCatching {
            FontStyle.valueOf(data.hudFont)
        }.getOrElse { FontStyle.VANILLA }
    }

    fun setUiMode(mode: HudUiMode) {
        save(currentData.copy(uiMode = mode))
    }

    fun customSceneName(engineKey: String, mode: HudSceneMode): String? =
        currentData.customScenes[engineKey.lowercase()]?.get(mode)

    fun setCustomSceneName(engineKey: String, mode: HudSceneMode, name: String) {
        val key = engineKey.lowercase()
        val currentSelection = currentData.customScenes[key] ?: CustomSceneSelection()
        save(
            currentData.copy(
                uiMode = HudUiMode.CUSTOM,
                customScenes = currentData.customScenes + (key to currentSelection.with(mode, name))
            )
        )
    }

    fun removeCustomSceneName(engineKey: String, mode: HudSceneMode) {
        val key = engineKey.lowercase()
        val currentSelection = currentData.customScenes[key] ?: return
        val nextSelection = currentSelection.without(mode)
        val nextScenes =
            if (nextSelection.isEmpty()) currentData.customScenes - key
            else currentData.customScenes + (key to nextSelection)

        save(currentData.copy(customScenes = nextScenes))
    }

}
