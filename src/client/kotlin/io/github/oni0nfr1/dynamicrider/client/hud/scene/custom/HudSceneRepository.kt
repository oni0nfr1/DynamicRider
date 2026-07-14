package io.github.oni0nfr1.dynamicrider.client.hud.scene.custom

import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * config override와 리소스팩 장면을 단일 우선순위로 조회하고 커스텀 파일을 관리한다.
 *
 * 조회 순서는 config, 엔진별 리소스, 모드별 기본 리소스 순이다.
 */
class HudSceneRepository(
    private val customRoot: Path,
) {
    /**
     * 현재 모드와 엔진에 적용할 HUD 장면을 결정하고 런타임 장면을 생성한다.
     *
     * 손상된 config는 보존하며 진단을 첨부한 뒤 리소스 장면으로 fallback한다.
     *
     * @return 선택된 장면과 출처 또는 모든 후보가 실패한 경우 오류 목록
     */
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

    /**
     * 장면 명세를 해당 모드와 엔진의 config override로 원자적으로 저장한다.
     *
     * 원자적 이동을 지원하지 않는 파일 시스템에서는 일반 교체 이동으로 재시도한다.
     * 모든 I/O 실패는 예외로 던지지 않고 [Result.failure]에 담아 반환한다.
     *
     * @return 성공 시 저장된 파일 경로, 실패 시 발생한 예외
     */
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

    /**
     * 해당 모드와 엔진의 config override를 삭제한다.
     *
     * @return 성공 시 실제 파일 삭제 여부, 실패 시 발생한 I/O 예외
     */
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
