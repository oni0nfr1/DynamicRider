package io.github.oni0nfr1.dynamicrider.client.hud.scene.config

import io.github.oni0nfr1.dynamicrider.client.util.warnLog
import kotlinx.serialization.json.Json
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

object CustomHudSceneLoader {

    private val json = Json {
        serializersModule = HudElementSerializersModule
        classDiscriminator = "type"
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    val uiDir: Path =
        FabricLoader.getInstance().configDir.resolve("dynamicrider").resolve("ui")

    fun load(name: String): LoadCustomHudResult {
        val fileName = normalizeName(name)
            ?: return LoadCustomHudResult.Failure(name, "UI name may only contain letters, digits, '.', '_' and '-'.")
        val path = filePath(fileName)

        if (!path.exists()) {
            return LoadCustomHudResult.Failure(
                fileName = fileName,
                reason = "UI file does not exist: ${path.fileName}",
                kind = LoadFailureKind.NOT_FOUND,
            )
        }

        val spec = try {
            json.decodeFromString<CustomHudSceneSpec>(path.readText())
        } catch (exception: Exception) {
            warnLog("Failed to load custom HUD scene '$fileName': ${exception.message}")
            return LoadCustomHudResult.Failure(fileName, exception.message ?: exception::class.java.simpleName)
        }

        val engineType = parseEngineType(spec.engine)
            ?: return LoadCustomHudResult.Failure(fileName, "Unknown engine '${spec.engine}'.")
        val key = HudSceneKey(engineType, spec.mode)

        val errors = validate(key, spec)
        if (errors.isNotEmpty()) {
            return LoadCustomHudResult.Failure(fileName, errors.joinToString("; "))
        }

        return LoadCustomHudResult.Success(
            LoadedCustomHudScene(
                fileName = fileName,
                displayName = spec.name,
                key = key,
                spec = spec,
            )
        )
    }

    fun list(): List<UiFileEntry> {
        Files.createDirectories(uiDir)

        return Files.list(uiDir).use { stream ->
            stream
                .filter { it.isRegularFile() && it.name.endsWith(".json") }
                .sorted()
                .map { path ->
                    val fileName = path.name.removeSuffix(".json")
                    try {
                        val header = json.decodeFromString<CustomHudSceneHeader>(path.readText())
                        UiFileEntry(
                            fileName = fileName,
                            displayName = header.name,
                            engine = header.engine,
                            mode = header.mode,
                        )
                    } catch (exception: Exception) {
                        UiFileEntry(
                            fileName = fileName,
                            displayName = null,
                            engine = null,
                            mode = null,
                            error = exception.message ?: exception::class.java.simpleName,
                        )
                    }
                }
                .toList()
        }
    }

    fun ensureDefaultFor(key: HudSceneKey): LoadCustomHudResult {
        val fileName = defaultCustomSceneName(key)
        val path = filePath(fileName)

        if (!path.exists()) {
            Files.createDirectories(uiDir)
            path.writeText(json.encodeToString(BuiltinHudScenes.defaultCustomSpec(key)))
        }

        return load(fileName)
    }

    fun defaultExists(key: HudSceneKey): Boolean =
        filePath(defaultCustomSceneName(key)).exists()

    private fun filePath(fileName: String): Path =
        uiDir.resolve("$fileName.json")

    private fun normalizeName(name: String): String? {
        val trimmed = name.removeSuffix(".json").trim()
        if (trimmed.isEmpty()) return null
        if (!trimmed.matches(Regex("[A-Za-z0-9_.-]+"))) return null
        return trimmed
    }

    private fun validate(key: HudSceneKey, spec: CustomHudSceneSpec): List<String> =
        spec.elements.mapIndexedNotNull { index, element ->
            val required = element.requiredEngineClass()
            val actual = key.engine.clazz

            if (required.isAssignableFrom(actual)) null
            else "Element[$index] requires ${required.simpleName}, but engine '${key.engineKey}' provides ${actual.simpleName}"
        }
}
