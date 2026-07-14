package io.github.oni0nfr1.dynamicrider.client.hud.scene.custom

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneSpecAddResult
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object HudSceneLoader {
    /**
     * 파일에서 장면 명세를 읽고 지정한 카트 엔진에 사용할 런타임 장면을 생성한다.
     *
     * 파일 접근, 역직렬화 및 엔진 호환성 오류는 예외 대신 [HudSceneLoadResult.Failed]로 반환한다.
     *
     * @param path 읽을 config 파일 경로
     * @param kart 런타임 요소에 전달할 특정 엔진 카트 참조
     * @param engineClass 장면 요소의 호환성을 검사할 엔진 클래스
     */
    fun <E : KartEngine> load(
        path: Path,
        kart: KartRef.Specific<E>,
        engineClass: Class<E>,
    ): HudSceneLoadResult<E> {
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

        return load(spec, path, kart, engineClass)
    }

    /**
     * 이미 역직렬화된 장면 명세를 검증하고 런타임 장면으로 변환한다.
     *
     * @param spec 변환할 장면 명세
     * @param sourcePath 오류 진단에 표시할 원본 경로
     * @param kart 런타임 요소에 전달할 특정 엔진 카트 참조
     * @param engineClass 장면 요소의 호환성을 검사할 엔진 클래스
     */
    fun <E : KartEngine> load(
        spec: HudSceneSpec,
        sourcePath: Path,
        kart: KartRef.Specific<E>,
        engineClass: Class<E>,
    ): HudSceneLoadResult<E> {
        val compatibilityErrors = spec.elements.mapIndexedNotNull { elementIndex, elementSpec ->
            val requiredEngineClass = elementSpec.requiredEngineClass()
            if (requiredEngineClass.isAssignableFrom(engineClass)) {
                null
            } else {
                HudSceneLoadError.IncompatibleElement(
                    path = sourcePath,
                    elementIndex = elementIndex,
                    specType = elementSpec::class.java.name,
                    requiredEngineClass = requiredEngineClass,
                    sceneEngineClass = engineClass,
                )
            }
        }

        if (compatibilityErrors.isNotEmpty()) {
            return HudSceneLoadResult.Failed(compatibilityErrors)
        }

        val scene = HudScene(kart, engineClass)
        spec.elements.forEachIndexed { elementIndex, elementSpec ->
            when (val result = scene.addSpecChecked(elementSpec)) {
                HudSceneSpecAddResult.Added -> Unit
                is HudSceneSpecAddResult.IncompatibleEngine -> {
                    return HudSceneLoadResult.Failed(
                        listOf(
                            HudSceneLoadError.IncompatibleElement(
                                path = sourcePath,
                                elementIndex = elementIndex,
                                specType = result.specType,
                                requiredEngineClass = result.requiredEngineClass,
                                sceneEngineClass = result.sceneEngineClass,
                            )
                        )
                    )
                }
            }
        }

        return HudSceneLoadResult.Loaded(scene)
    }
}
