package io.github.oni0nfr1.dynamicrider.client.hud.scene.config

import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.layouts.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.layouts.hudScene
import io.github.oni0nfr1.skid.client.api.engine.A2Engine
import io.github.oni0nfr1.skid.client.api.engine.BoatEngine
import io.github.oni0nfr1.skid.client.api.engine.ChargeEngine
import io.github.oni0nfr1.skid.client.api.engine.EXEngine
import io.github.oni0nfr1.skid.client.api.engine.F1Engine
import io.github.oni0nfr1.skid.client.api.engine.GearEngine
import io.github.oni0nfr1.skid.client.api.engine.JiuEngine
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.engine.KeyEngine
import io.github.oni0nfr1.skid.client.api.engine.LegacyEngine
import io.github.oni0nfr1.skid.client.api.engine.MKEngine
import io.github.oni0nfr1.skid.client.api.engine.N1Engine
import io.github.oni0nfr1.skid.client.api.engine.NewEngine
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.engine.ProEngine
import io.github.oni0nfr1.skid.client.api.engine.RXEngine
import io.github.oni0nfr1.skid.client.api.engine.RallyEngine
import io.github.oni0nfr1.skid.client.api.engine.RushPlusEngine
import io.github.oni0nfr1.skid.client.api.engine.SREngine
import io.github.oni0nfr1.skid.client.api.engine.V1Engine
import io.github.oni0nfr1.skid.client.api.engine.XEngine
import io.github.oni0nfr1.skid.client.api.engine.Z7Engine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
enum class HudUiMode {
    DEFAULT,
    CUSTOM,
}

@Serializable
enum class HudSceneMode {
    RIDE,
    SPECTATE,
}

@Serializable
data class CustomSceneSelection(
    val ride: String? = null,
    val spectate: String? = null,
) {
    fun get(mode: HudSceneMode): String? =
        when (mode) {
            HudSceneMode.RIDE -> ride
            HudSceneMode.SPECTATE -> spectate
        }

    fun with(mode: HudSceneMode, name: String): CustomSceneSelection =
        when (mode) {
            HudSceneMode.RIDE -> copy(ride = name)
            HudSceneMode.SPECTATE -> copy(spectate = name)
        }

    fun without(mode: HudSceneMode): CustomSceneSelection =
        when (mode) {
            HudSceneMode.RIDE -> copy(ride = null)
            HudSceneMode.SPECTATE -> copy(spectate = null)
        }

    fun isEmpty(): Boolean = ride == null && spectate == null
}

@Serializable
data class CustomHudSceneSpec(
    val name: String,
    val engine: String,
    val mode: HudSceneMode = HudSceneMode.RIDE,
    val elements: List<@Polymorphic HudElementSpec<*, *>>,
) {
    fun toHudScene(kart: KartRef.Specific<NitroEngine>): HudScene<NitroEngine> =
        hudScene(kart) {
            elements.forEach { spec ->
                @Suppress("UNCHECKED_CAST")
                addSpec(spec as HudElementSpec<*, NitroEngine>)
            }
        }
}

@Serializable
data class CustomHudSceneHeader(
    val name: String,
    val engine: String,
    val mode: HudSceneMode = HudSceneMode.RIDE,
)

data class HudSceneKey(
    val engine: KartEngine.Type,
    val mode: HudSceneMode,
) {
    val engineKey: String
        get() = engine.configKey
}

data class LoadedCustomHudScene(
    val fileName: String,
    val displayName: String,
    val key: HudSceneKey,
    val spec: CustomHudSceneSpec,
)

sealed interface LoadCustomHudResult {
    data class Success(val scene: LoadedCustomHudScene) : LoadCustomHudResult
    data class Failure(
        val fileName: String,
        val reason: String,
        val kind: LoadFailureKind = LoadFailureKind.INVALID,
    ) : LoadCustomHudResult
}

enum class LoadFailureKind {
    NOT_FOUND,
    INVALID,
}

data class UiFileEntry(
    val fileName: String,
    val displayName: String?,
    val engine: String?,
    val mode: HudSceneMode?,
    val error: String? = null,
)

val KartEngine.Type.configKey: String
    get() = engineName.lowercase()

fun parseEngineType(raw: String): KartEngine.Type? {
    val key = raw.lowercase()
    return KartEngine.Type.entries.firstOrNull {
        it.engineName.lowercase() == key || it.name.lowercase() == key
    }
}

val KartEngine.Type.clazz: Class<out KartEngine>
    get() = when (this) {
        KartEngine.Type.X -> XEngine::class.java
        KartEngine.Type.EX -> EXEngine::class.java
        KartEngine.Type.JIU -> JiuEngine::class.java
        KartEngine.Type.NEW -> NewEngine::class.java
        KartEngine.Type.Z7 -> Z7Engine::class.java
        KartEngine.Type.V1 -> V1Engine::class.java
        KartEngine.Type.A2 -> A2Engine::class.java
        KartEngine.Type.LEGACY -> LegacyEngine::class.java
        KartEngine.Type.PRO -> ProEngine::class.java
        KartEngine.Type.RUSHPLUS -> RushPlusEngine::class.java
        KartEngine.Type.CHARGE -> ChargeEngine::class.java
        KartEngine.Type.SR -> SREngine::class.java
        KartEngine.Type.N1 -> N1Engine::class.java
        KartEngine.Type.RX -> RXEngine::class.java
        KartEngine.Type.KEY -> KeyEngine::class.java
        KartEngine.Type.GEAR -> GearEngine::class.java
        KartEngine.Type.F1 -> F1Engine::class.java
        KartEngine.Type.RALLY -> RallyEngine::class.java
        KartEngine.Type.MK -> MKEngine::class.java
        KartEngine.Type.BOAT -> BoatEngine::class.java
    }

fun defaultCustomSceneName(key: HudSceneKey): String =
    when (key.mode) {
        HudSceneMode.RIDE -> key.engineKey
        HudSceneMode.SPECTATE -> "${key.engineKey}_spectate"
    }
