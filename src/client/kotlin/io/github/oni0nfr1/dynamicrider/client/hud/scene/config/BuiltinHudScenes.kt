package io.github.oni0nfr1.dynamicrider.client.hud.scene.config

import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.layouts.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.layouts.defaultScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.layouts.defaultSceneDefinition
import io.github.oni0nfr1.dynamicrider.client.hud.scene.layouts.defaultSpectateScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.layouts.defaultSpectateSceneDefinition
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

object BuiltinHudScenes {

    fun create(key: HudSceneKey, kart: KartRef.Specific<NitroEngine>): HudScene<NitroEngine> =
        when (key.mode) {
            HudSceneMode.RIDE -> defaultScene(kart)
            HudSceneMode.SPECTATE -> defaultSpectateScene(kart)
        }

    fun defaultCustomSpec(key: HudSceneKey): CustomHudSceneSpec {
        val specs: List<HudElementSpec<*, *>> =
            when (key.mode) {
                HudSceneMode.RIDE -> defaultSceneDefinition().specs
                HudSceneMode.SPECTATE -> defaultSpectateSceneDefinition().specs
            }.map {
                @Suppress("UNCHECKED_CAST")
                it as HudElementSpec<*, *>
            }

        return CustomHudSceneSpec(
            name = displayName(key),
            engine = key.engineKey,
            mode = key.mode,
            elements = specs,
        )
    }

    fun displayName(key: HudSceneKey): String =
        when (key.mode) {
            HudSceneMode.RIDE -> "builtin/${key.engineKey}"
            HudSceneMode.SPECTATE -> "builtin/${key.engineKey}_spectate"
        }
}
