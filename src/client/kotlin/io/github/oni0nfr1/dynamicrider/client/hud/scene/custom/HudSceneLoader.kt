package io.github.oni0nfr1.dynamicrider.client.hud.scene.custom

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneSpecAddResult
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object HudSceneLoader {
    private val json = Json {
        serializersModule = HudElementSerializersModule
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

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
            json.decodeFromString<HudSceneSpec>(content)
        } catch (exception: SerializationException) {
            return HudSceneLoadResult.Failed(listOf(HudSceneLoadError.DecodeFailure(path, exception)))
        }

        val compatibilityErrors = spec.elements.mapIndexedNotNull { elementIndex, elementSpec ->
            val requiredEngineClass = elementSpec.requiredEngineClass()
            if (requiredEngineClass.isAssignableFrom(engineClass)) {
                null
            } else {
                HudSceneLoadError.IncompatibleElement(
                    path = path,
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
                                path = path,
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
