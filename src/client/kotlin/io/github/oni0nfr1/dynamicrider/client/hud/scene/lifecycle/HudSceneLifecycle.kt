package io.github.oni0nfr1.dynamicrider.client.hud.scene.lifecycle

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.ride.*
import io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.spectate.*
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HudSceneLoadError
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HudSceneLoadResult
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HudSceneLoader
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HudScenePaths
import io.github.oni0nfr1.dynamicrider.client.util.chatLog
import io.github.oni0nfr1.dynamicrider.client.util.debugLog
import io.github.oni0nfr1.dynamicrider.client.util.warnLog
import io.github.oni0nfr1.skid.client.api.engine.*
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import java.nio.file.Path

object HudSceneLifecycle {
    private val customSceneRoot: Path
        get() = FabricLoader.getInstance().configDir.resolve("dynrider")

    fun createRideScene(kart: KartRef): HudScene<out KartEngine>? =
        kart.access {
            return@access when (val currentEngine = engine ?: return@access null) {
                is XEngine -> loadSpecificRideScene(currentEngine, ::defaultXRideScene)
                is EXEngine -> loadSpecificRideScene(currentEngine, ::defaultEXRideScene)
                is JiuEngine -> loadSpecificRideScene(currentEngine, ::defaultJiuRideScene)
                is NewEngine -> loadSpecificRideScene(currentEngine, ::defaultNewRideScene)
                is Z7Engine -> loadSpecificRideScene(currentEngine, ::defaultZ7RideScene)
                is V1Engine -> loadSpecificRideScene(currentEngine, ::defaultV1RideScene)
                is A2Engine -> loadSpecificRideScene(currentEngine, ::defaultA2RideScene)
                is LegacyEngine -> loadSpecificRideScene(currentEngine, ::defaultLegacyRideScene)
                is ProEngine -> loadSpecificRideScene(currentEngine, ::defaultProRideScene)
                is RushPlusEngine -> loadSpecificRideScene(currentEngine, ::defaultRushPlusRideScene)
                is ChargeEngine -> loadSpecificRideScene(currentEngine, ::defaultChargeRideScene)
                is SREngine -> loadSpecificRideScene(currentEngine, ::defaultSRRideScene)
                is N1Engine -> loadSpecificRideScene(currentEngine, ::defaultN1RideScene)
                is RXEngine -> loadSpecificRideScene(currentEngine, ::defaultRXRideScene)
                is KeyEngine -> loadSpecificRideScene(currentEngine, ::defaultKeyRideScene)
                is GearEngine -> loadSpecificRideScene(currentEngine, ::defaultGearRideScene)
                is F1Engine -> loadSpecificRideScene(currentEngine, ::defaultF1RideScene)
                is RallyEngine -> loadSpecificRideScene(currentEngine, ::defaultRallyRideScene)
                is MKEngine -> loadSpecificRideScene(currentEngine, ::defaultMKRideScene)
                is BoatEngine -> loadSpecificRideScene(currentEngine, ::defaultBoatRideScene)
            }
        }

    fun createSpectateScene(kart: KartRef): HudScene<out KartEngine>? =
        kart.access {
            return@access when (val currentEngine = engine ?: return@access null) {
                is XEngine -> loadSpecificSpectateScene(currentEngine, ::defaultXSpectateScene)
                is EXEngine -> loadSpecificSpectateScene(currentEngine, ::defaultEXSpectateScene)
                is JiuEngine -> loadSpecificSpectateScene(currentEngine, ::defaultJiuSpectateScene)
                is NewEngine -> loadSpecificSpectateScene(currentEngine, ::defaultNewSpectateScene)
                is Z7Engine -> loadSpecificSpectateScene(currentEngine, ::defaultZ7SpectateScene)
                is V1Engine -> loadSpecificSpectateScene(currentEngine, ::defaultV1SpectateScene)
                is A2Engine -> loadSpecificSpectateScene(currentEngine, ::defaultA2SpectateScene)
                is LegacyEngine -> loadSpecificSpectateScene(currentEngine, ::defaultLegacySpectateScene)
                is ProEngine -> loadSpecificSpectateScene(currentEngine, ::defaultProSpectateScene)
                is RushPlusEngine -> loadSpecificSpectateScene(currentEngine, ::defaultRushPlusSpectateScene)
                is ChargeEngine -> loadSpecificSpectateScene(currentEngine, ::defaultChargeSpectateScene)
                is SREngine -> loadSpecificSpectateScene(currentEngine, ::defaultSRSpectateScene)
                is N1Engine -> loadSpecificSpectateScene(currentEngine, ::defaultN1SpectateScene)
                is RXEngine -> loadSpecificSpectateScene(currentEngine, ::defaultRXSpectateScene)
                is KeyEngine -> loadSpecificSpectateScene(currentEngine, ::defaultKeySpectateScene)
                is GearEngine -> loadSpecificSpectateScene(currentEngine, ::defaultGearSpectateScene)
                is F1Engine -> loadSpecificSpectateScene(currentEngine, ::defaultF1SpectateScene)
                is RallyEngine -> loadSpecificSpectateScene(currentEngine, ::defaultRallySpectateScene)
                is MKEngine -> loadSpecificSpectateScene(currentEngine, ::defaultMKSpectateScene)
                is BoatEngine -> loadSpecificSpectateScene(currentEngine, ::defaultBoatSpectateScene)
            }
        }

    private inline fun <reified E : KartEngine> loadSpecificRideScene(
        engine: E,
        noinline builtinScene: (KartRef.Specific<E>) -> HudScene<E>,
    ): HudScene<E> {
        val kart = KartRef.specify(engine)
        val path = HudScenePaths.customHudScenePath(customSceneRoot, HudSceneMode.RIDE, engine.type)
        return when (val result = HudSceneLoader.load(path, kart, E::class.java)) {
            is HudSceneLoadResult.Loaded -> result.scene
            is HudSceneLoadResult.Failed -> {
                reportLoadFailure(result.errors)
                builtinScene(kart)
            }
        }
    }

    private inline fun <reified E : KartEngine> loadSpecificSpectateScene(
        engine: E,
        noinline builtinScene: (KartRef.Specific<E>) -> HudScene<E>,
    ): HudScene<E> {
        val kart = KartRef.specify(engine)
        val path = HudScenePaths.customHudScenePath(customSceneRoot, HudSceneMode.SPECTATE, engine.type)
        return when (val result = HudSceneLoader.load(path, kart, E::class.java)) {
            is HudSceneLoadResult.Loaded -> result.scene
            is HudSceneLoadResult.Failed -> {
                reportLoadFailure(result.errors)
                builtinScene(kart)
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
                displayEngineName(error.sceneEngineClass),
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
                    "sceneEngine=${error.sceneEngineClass.name}, requiredEngine=${error.requiredEngineClass.name}, " +
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

    private fun displayEngineName(engineClass: Class<out KartEngine>): String {
        return engineClass.simpleName.removeSuffix("Engine").uppercase()
    }

    private fun displayElementName(specType: String): String {
        return specType.substringAfterLast('.').replace('$', '.')
    }
}
