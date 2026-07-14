package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.nio.file.Path

sealed interface HudSceneLoadError {
    data class FileNotFound(
        val path: Path,
    ) : HudSceneLoadError

    data class IoFailure(
        val path: Path,
        val cause: IOException,
    ) : HudSceneLoadError

    data class DecodeFailure(
        val path: Path,
        val cause: SerializationException,
    ) : HudSceneLoadError

    data class IncompatibleElement(
        val path: Path,
        val elementIndex: Int,
        val specType: String,
        val requiredStateClass: Class<out KartState>,
        val sceneStateClass: Class<out KartState>,
    ) : HudSceneLoadError
}
