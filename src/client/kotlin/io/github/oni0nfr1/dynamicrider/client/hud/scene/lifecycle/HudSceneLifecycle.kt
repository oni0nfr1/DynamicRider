package io.github.oni0nfr1.dynamicrider.client.hud.scene.lifecycle

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HudSceneLoadError
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HudSceneRepository
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HudSceneResolution
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HudSceneMode
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

    private val repository: HudSceneRepository by lazy { HudSceneRepository(customSceneRoot) }

    fun createRideScene(kart: KartRef): HudScene<out KartEngine>? =
        kart.access {
            return@access when (val currentEngine = engine ?: return@access null) {
                is XEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is EXEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is JiuEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is NewEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is Z7Engine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is V1Engine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is A2Engine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is LegacyEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is ProEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is RushPlusEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is ChargeEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is SREngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is N1Engine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is RXEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is KeyEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is GearEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is F1Engine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is RallyEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is MKEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
                is BoatEngine -> loadSpecificScene(HudSceneMode.RIDE, currentEngine)
            }
        }

    fun createSpectateScene(kart: KartRef): HudScene<out KartEngine>? =
        kart.access {
            return@access when (val currentEngine = engine ?: return@access null) {
                is XEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is EXEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is JiuEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is NewEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is Z7Engine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is V1Engine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is A2Engine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is LegacyEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is ProEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is RushPlusEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is ChargeEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is SREngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is N1Engine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is RXEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is KeyEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is GearEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is F1Engine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is RallyEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is MKEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
                is BoatEngine -> loadSpecificScene(HudSceneMode.SPECTATE, currentEngine)
            }
        }

    private inline fun <reified E : KartEngine> loadSpecificScene(
        mode: HudSceneMode,
        engine: E,
    ): HudScene<E> {
        val kart = KartRef.specify(engine)
        return when (val result = repository.resolve(mode, engine.type, kart, E::class.java)) {
            is HudSceneResolution.Resolved -> {
                if (result.diagnostics.isNotEmpty()) reportLoadFailure(result.diagnostics)
                result.scene
            }
            is HudSceneResolution.Failed -> {
                reportLoadFailure(result.errors)
                HudScene(kart, E::class.java)
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
