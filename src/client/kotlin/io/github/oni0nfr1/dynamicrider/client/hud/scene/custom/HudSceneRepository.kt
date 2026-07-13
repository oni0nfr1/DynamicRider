package io.github.oni0nfr1.dynamicrider.client.hud.scene.custom

import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class HudSceneRepository(
    private val customRoot: Path,
) {
    fun <E : KartEngine> resolve(
        mode: HudSceneMode,
        engineType: KartEngine.Type,
        kart: KartRef.Specific<E>,
        engineClass: Class<E>,
    ): HudSceneResolution<E> {
        val customPath = HudScenePaths.customHudScenePath(customRoot, mode, engineType)
        val customResult = HudSceneLoader.load(customPath, kart, engineClass)
        when (customResult) {
            is HudSceneLoadResult.Loaded -> return HudSceneResolution.Resolved(
                scene = customResult.scene,
                source = HudSceneSource.CUSTOM_CONFIG,
            )

            is HudSceneLoadResult.Failed -> {
                val onlyMissing = customResult.errors.all { it is HudSceneLoadError.FileNotFound }
                val diagnostics = if (onlyMissing) emptyList() else customResult.errors
                return resolveResource(mode, engineType, kart, engineClass, diagnostics)
            }
        }
    }

    fun saveCustom(
        mode: HudSceneMode,
        engineType: KartEngine.Type,
        spec: HudSceneSpec,
    ): Result<Path> = runCatching {
        val path = HudScenePaths.customHudScenePath(customRoot, mode, engineType)
        Files.createDirectories(path.parent)
        val temporaryPath = Files.createTempFile(path.parent, "${path.fileName}.", ".tmp")
        try {
            Files.writeString(temporaryPath, HudSceneCodec.encode(spec))
            try {
                Files.move(
                    temporaryPath,
                    path,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING,
                )
            } catch (_: AtomicMoveNotSupportedException) {
                Files.move(temporaryPath, path, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporaryPath)
        }
        path
    }

    fun deleteCustom(mode: HudSceneMode, engineType: KartEngine.Type): Result<Boolean> =
        runCatching {
            Files.deleteIfExists(HudScenePaths.customHudScenePath(customRoot, mode, engineType))
        }

    private fun <E : KartEngine> resolveResource(
        mode: HudSceneMode,
        engineType: KartEngine.Type,
        kart: KartRef.Specific<E>,
        engineClass: Class<E>,
        customDiagnostics: List<HudSceneLoadError>,
    ): HudSceneResolution<E> {
        val engineResourceId = HudScenePaths.resourceHudSceneId(mode, engineType)
        val engineResourceError = HudSceneResourceRegistry.getLoadError(engineResourceId)
        val useDefault = HudSceneResourceRegistry.get(engineResourceId) == null && engineResourceError == null
        val resourceId = if (useDefault) {
            HudScenePaths.defaultResourceHudSceneId(mode)
        } else {
            engineResourceId
        }
        val resourcePath = if (useDefault) {
            HudScenePaths.defaultResourceDisplayPath(mode)
        } else {
            HudScenePaths.resourceDisplayPath(mode, engineType)
        }
        val spec = HudSceneResourceRegistry.get(resourceId)
        if (spec == null) {
            val resourceError = HudSceneResourceRegistry.getLoadError(resourceId)?.let {
                HudSceneLoadError.DecodeFailure(resourcePath, it)
            } ?: HudSceneLoadError.FileNotFound(resourcePath)
            return HudSceneResolution.Failed(customDiagnostics + resourceError)
        }

        return when (val result = HudSceneLoader.load(spec, resourcePath, kart, engineClass)) {
            is HudSceneLoadResult.Loaded -> HudSceneResolution.Resolved(
                scene = result.scene,
                source = if (customDiagnostics.isEmpty()) {
                    HudSceneSource.RESOURCE
                } else {
                    HudSceneSource.CUSTOM_FALLBACK
                },
                diagnostics = customDiagnostics,
            )

            is HudSceneLoadResult.Failed -> HudSceneResolution.Failed(
                customDiagnostics + result.errors
            )
        }
    }
}
