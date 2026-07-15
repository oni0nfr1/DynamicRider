package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneMutationResult
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidator
import java.nio.file.Path

object HudSceneLoader {
    /**
     * 이미 역직렬화된 장면 명세를 검증하고 런타임 장면으로 변환한다.
     *
     * @param spec 변환할 장면 명세
     * @param sourcePath 오류 진단에 표시할 원본 경로
     * @param context 런타임 요소에 전달하고 상태 호환성을 검사할 장면 context
     */
    fun <S : KartState> load(
        spec: HudSceneSpec,
        sourcePath: Path,
        context: HudSceneContext<S>,
    ): HudSceneLoadResult<S> {
        val validationErrors = HudSpecValidator.validateScene(spec, context.kartStateType)
            .toLoadErrors(sourcePath)
        if (validationErrors.isNotEmpty()) return HudSceneLoadResult.Failed(validationErrors)

        val scene = HudScene(context)
        spec.elements.forEachIndexed { elementIndex, elementSpec ->
            val elementId = spec.elementIds.getOrNull(elementIndex) ?: "element-${elementIndex + 1}"
            when (val result = scene.addElement(elementId, elementSpec)) {
                HudSceneMutationResult.Applied -> Unit
                is HudSceneMutationResult.IncompatibleState -> {
                    return HudSceneLoadResult.Failed(
                        listOf(
                            HudSceneLoadError.IncompatibleElement(
                                path = sourcePath,
                                elementIndex = elementIndex,
                                specType = result.specType,
                                requiredStateClass = result.requiredStateClass,
                                sceneStateClass = result.sceneStateClass,
                            )
                        )
                    )
                }
                is HudSceneMutationResult.InvalidSpec -> {
                    return HudSceneLoadResult.Failed(
                        listOf(
                            HudSceneLoadError.InvalidElement(
                                path = sourcePath,
                                elementIndex = elementIndex,
                                elementId = spec.elementIds.getOrNull(elementIndex),
                                specType = elementSpec::class.java.name,
                                errors = result.errors,
                            )
                        )
                    )
                }
                is HudSceneMutationResult.DuplicateElementId,
                is HudSceneMutationResult.ElementNotFound -> error(
                    "Unexpected HUD scene mutation result while adding '$elementId': $result"
                )
            }
        }

        return HudSceneLoadResult.Loaded(scene)
    }
}
