package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidator
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * config override와 리소스팩 장면을 단일 우선순위로 조회하고 커스텀 파일을 관리한다.
 *
 * 조회 순서는 config, 상태 타입별 리소스, 모드별 기본 리소스 순이다.
 */
class HudSceneRepository(
    private val customRoot: Path,
) {
    /**
     * 현재 모드와 상태 타입에 적용할 HUD 장면 명세를 결정한다.
     *
     * 손상된 config는 보존하며 진단을 첨부한 뒤 리소스 장면으로 fallback한다.
     *
     * @return 선택된 명세, 출처와 진단 또는 모든 후보가 실패한 경우 오류 목록
     */
    fun resolveSpec(
        mode: HudSceneMode,
        stateType: KartStateType<*>,
    ): HudSceneSpecResolution {
        val customPath = HudScenePaths.customHudScenePath(customRoot, mode, stateType)
        when (val customResult = loadCandidate(customPath, stateType)) {
            is CandidateResult.Loaded -> return HudSceneSpecResolution.Resolved(
                spec = customResult.spec,
                sourcePath = customPath,
                source = HudSceneSource.CUSTOM_CONFIG,
            )

            is CandidateResult.Failed -> {
                val onlyMissing = customResult.errors.all { it is HudSceneLoadError.FileNotFound }
                val diagnostics = if (onlyMissing) emptyList() else customResult.errors
                return resolveResourceSpec(mode, stateType, diagnostics)
            }
        }
    }

    /**
     * 장면 명세를 해당 모드와 상태 타입의 config override로 원자적으로 저장한다.
     *
     * 원자적 이동을 지원하지 않는 파일 시스템에서는 일반 교체 이동으로 재시도한다.
     * 모든 I/O 실패는 예외로 던지지 않고 [Result.failure]에 담아 반환한다.
     *
     * @return 성공 시 저장된 파일 경로, 실패 시 발생한 예외
     */
    fun saveCustom(
        mode: HudSceneMode,
        stateType: KartStateType<*>,
        spec: HudSceneSpec,
    ): Result<Path> = runCatching {
        val path = HudScenePaths.customHudScenePath(customRoot, mode, stateType)
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
     * 해당 모드와 상태 타입의 config override를 삭제한다.
     *
     * @return 성공 시 실제 파일 삭제 여부, 실패 시 발생한 I/O 예외
     */
    fun deleteCustom(mode: HudSceneMode, stateType: KartStateType<*>): Result<Boolean> =
        runCatching {
            Files.deleteIfExists(HudScenePaths.customHudScenePath(customRoot, mode, stateType))
        }

    private fun resolveResourceSpec(
        mode: HudSceneMode,
        stateType: KartStateType<*>,
        customDiagnostics: List<HudSceneLoadError>,
    ): HudSceneSpecResolution {
        val stateResourceId = HudScenePaths.resourceHudSceneId(mode, stateType)
        val stateResourceError = HudSceneResourceRegistry.getLoadError(stateResourceId)
        val useDefault = HudSceneResourceRegistry.get(stateResourceId) == null && stateResourceError == null
        val resourceId = if (useDefault) {
            HudScenePaths.defaultResourceHudSceneId(mode)
        } else {
            stateResourceId
        }
        val resourcePath = if (useDefault) {
            HudScenePaths.defaultResourceDisplayPath(mode)
        } else {
            HudScenePaths.resourceDisplayPath(mode, stateType)
        }
        val spec = HudSceneResourceRegistry.get(resourceId)
        if (spec == null) {
            val resourceError = HudSceneResourceRegistry.getLoadError(resourceId)?.let {
                HudSceneLoadError.DecodeFailure(resourcePath, it)
            } ?: HudSceneLoadError.FileNotFound(resourcePath)
            return HudSceneSpecResolution.Failed(customDiagnostics + resourceError)
        }

        val validationErrors = HudSpecValidator.validateScene(spec, stateType)
            .toLoadErrors(resourcePath)
        return if (validationErrors.isEmpty()) {
            HudSceneSpecResolution.Resolved(
                spec = spec,
                sourcePath = resourcePath,
                source = if (customDiagnostics.isEmpty()) {
                    HudSceneSource.RESOURCE
                } else {
                    HudSceneSource.CUSTOM_FALLBACK
                },
                diagnostics = customDiagnostics,
            )
        } else {
            HudSceneSpecResolution.Failed(customDiagnostics + validationErrors)
        }
    }

    private fun loadCandidate(
        path: Path,
        stateType: KartStateType<*>,
    ): CandidateResult {
        if (!Files.exists(path)) {
            return CandidateResult.Failed(listOf(HudSceneLoadError.FileNotFound(path)))
        }
        val content = try {
            Files.readString(path)
        } catch (exception: IOException) {
            return CandidateResult.Failed(listOf(HudSceneLoadError.IoFailure(path, exception)))
        }
        val spec = try {
            HudSceneCodec.decode(content)
        } catch (exception: SerializationException) {
            return CandidateResult.Failed(listOf(HudSceneLoadError.DecodeFailure(path, exception)))
        }
        val validationErrors = HudSpecValidator.validateScene(spec, stateType)
            .toLoadErrors(path)
        return if (validationErrors.isEmpty()) {
            CandidateResult.Loaded(spec)
        } else {
            CandidateResult.Failed(validationErrors)
        }
    }

    private sealed interface CandidateResult {
        data class Loaded(val spec: HudSceneSpec) : CandidateResult
        data class Failed(val errors: List<HudSceneLoadError>) : CandidateResult
    }
}
