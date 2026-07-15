package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSceneValidationError
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSceneValidationResult
import java.nio.file.Path

/** 중립적인 장면 검증 결과에 원본 경로를 결합해 loader 진단으로 변환한다. */
internal fun HudSceneValidationResult.toLoadErrors(sourcePath: Path): List<HudSceneLoadError> = when (this) {
    HudSceneValidationResult.Valid -> emptyList()
    is HudSceneValidationResult.Invalid -> errors.map { it.toLoadError(sourcePath) }
}

private fun HudSceneValidationError.toLoadError(sourcePath: Path): HudSceneLoadError = when (this) {
    is HudSceneValidationError.IncompatibleState -> HudSceneLoadError.IncompatibleElement(
        path = sourcePath,
        elementIndex = elementIndex,
        specType = specType,
        requiredStateClass = requiredStateClass,
        sceneStateClass = sceneStateClass,
    )
    is HudSceneValidationError.InvalidSpec -> HudSceneLoadError.InvalidElement(
        path = sourcePath,
        elementIndex = elementIndex,
        elementId = elementId,
        specType = specType,
        errors = errors,
    )
}
