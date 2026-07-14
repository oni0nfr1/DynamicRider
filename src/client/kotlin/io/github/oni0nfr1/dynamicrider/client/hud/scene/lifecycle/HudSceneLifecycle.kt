package io.github.oni0nfr1.dynamicrider.client.hud.scene.lifecycle

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneLoadError
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneRepository
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneResolution
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.util.chatLog
import io.github.oni0nfr1.dynamicrider.client.util.debugLog
import io.github.oni0nfr1.dynamicrider.client.util.warnLog
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import java.nio.file.Path

object HudSceneLifecycle {
    private val customSceneRoot: Path
        get() = FabricLoader.getInstance().configDir.resolve("dynrider")

    private val repository: HudSceneRepository by lazy { HudSceneRepository(customSceneRoot) }

    fun <S : KartState> createRideScene(context: HudSceneContext<S>): HudScene<S> =
        loadScene(HudSceneMode.RIDE, context)

    fun <S : KartState> createSpectateScene(context: HudSceneContext<S>): HudScene<S> =
        loadScene(HudSceneMode.SPECTATE, context)

    private fun <S : KartState> loadScene(
        mode: HudSceneMode,
        context: HudSceneContext<S>,
    ): HudScene<S> {
        return when (val result = repository.resolve(mode, context)) {
            is HudSceneResolution.Resolved -> {
                if (result.diagnostics.isNotEmpty()) reportLoadFailure(result.diagnostics)
                result.scene
            }
            is HudSceneResolution.Failed -> {
                reportLoadFailure(result.errors)
                HudScene(context)
            }
        }
    }

    private fun reportLoadFailure(errors: List<HudSceneLoadError>) {
        errors.forEach(::warnLoadFailure)

        errors.firstNotNullOfOrNull(::chatMessageForLoadFailure)?.let { chatLog(it) }
    }

    private fun chatMessageForLoadFailure(error: HudSceneLoadError): Component? {
        return when (error) {
            is HudSceneLoadError.FileNotFound -> null
            is HudSceneLoadError.IoFailure -> Component.translatable(
                "dynrider.error.scene.load_failed.io",
                displayPath(error.path),
            )

            is HudSceneLoadError.DecodeFailure -> Component.translatable(
                "dynrider.error.scene.load_failed.decode",
                displayPath(error.path),
            )

            is HudSceneLoadError.IncompatibleElement -> Component.translatable(
                "dynrider.error.scene.load_failed.incompatible",
                displayStateName(error.sceneStateClass),
                displayElementName(error.specType),
            )
        }
    }

    private fun warnLoadFailure(error: HudSceneLoadError) {
        when (error) {
            is HudSceneLoadError.FileNotFound -> debugLog(
                "Custom HUD scene file not found: path=${error.path}. Fallback to built-in scene."
            )

            is HudSceneLoadError.IoFailure -> warnLog(
                "Failed to access custom HUD scene file: path=${error.path}, cause=${error.cause::class.java.name}: ${error.cause.message}"
            )

            is HudSceneLoadError.DecodeFailure -> warnLog(
                "Failed to decode custom HUD scene file: path=${error.path}, cause=${error.cause::class.java.name}: ${error.cause.message}"
            )

            is HudSceneLoadError.IncompatibleElement -> warnLog(
                "Incompatible custom HUD element: path=${error.path}, index=${error.elementIndex}, " +
                    "sceneState=${error.sceneStateClass.name}, requiredState=${error.requiredStateClass.name}, " +
                    "spec=${error.specType}"
            )
        }
    }

    private fun displayPath(path: Path): String {
        return runCatching {
            customSceneRoot.toAbsolutePath().normalize()
                .relativize(path.toAbsolutePath().normalize())
                .toString()
                .replace('\\', '/')
        }.getOrElse {
            path.fileName?.toString() ?: path.toString()
        }
    }

    private fun displayStateName(stateClass: Class<out KartState>): String {
        return stateClass.simpleName.removeSuffix("KartState").uppercase()
    }

    private fun displayElementName(specType: String): String {
        return specType.substringAfterLast('.').replace('$', '.')
    }
}
