package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneSpecAddResult
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object HudSceneLoader {
    /**
     * 파일에서 장면 명세를 읽고 지정한 상태 context에 사용할 런타임 장면을 생성한다.
     *
     * 파일 접근, 역직렬화 및 상태 타입 호환성 오류는 예외 대신 [HudSceneLoadResult.Failed]로 반환한다.
     *
     * @param path 읽을 config 파일 경로
     * @param context 런타임 요소에 전달하고 상태 호환성을 검사할 장면 context
     */
    fun <S : KartState> load(
        path: Path,
        context: HudSceneContext<S>,
    ): HudSceneLoadResult<S> {
        if (!Files.exists(path)) {
            return HudSceneLoadResult.Failed(listOf(HudSceneLoadError.FileNotFound(path)))
        }

        val content = try {
            Files.readString(path)
        } catch (exception: IOException) {
            return HudSceneLoadResult.Failed(listOf(HudSceneLoadError.IoFailure(path, exception)))
        }

        val spec = try {
            HudSceneCodec.decode(content)
        } catch (exception: SerializationException) {
            return HudSceneLoadResult.Failed(listOf(HudSceneLoadError.DecodeFailure(path, exception)))
        }

        return load(spec, path, context)
    }

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
        val sceneStateClass = context.kartStateType.stateClass
        val compatibilityErrors = spec.elements.mapIndexedNotNull { elementIndex, elementSpec ->
            val requiredStateClass = elementSpec.requiredStateClass()
            if (requiredStateClass.isAssignableFrom(sceneStateClass)) {
                null
            } else {
                HudSceneLoadError.IncompatibleElement(
                    path = sourcePath,
                    elementIndex = elementIndex,
                    specType = elementSpec::class.java.name,
                    requiredStateClass = requiredStateClass,
                    sceneStateClass = sceneStateClass,
                )
            }
        }

        if (compatibilityErrors.isNotEmpty()) {
            return HudSceneLoadResult.Failed(compatibilityErrors)
        }

        val scene = HudScene(context)
        spec.elements.forEachIndexed { elementIndex, elementSpec ->
            when (val result = scene.addSpec(elementSpec)) {
                HudSceneSpecAddResult.Added -> Unit
                is HudSceneSpecAddResult.IncompatibleState -> {
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
                is HudSceneSpecAddResult.InvalidSpec -> {
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
            }
        }

        return HudSceneLoadResult.Loaded(scene)
    }
}
