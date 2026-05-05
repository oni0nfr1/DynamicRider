package io.github.oni0nfr1.dynamicrider.client.hud.scene

import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HudSceneResolver
import io.github.oni0nfr1.dynamicrider.client.hud.scene.layouts.HudScene
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

fun makeScene(engineType: KartEngine.Type, kart: KartRef.Specific<NitroEngine>): HudScene<NitroEngine> {
    return HudSceneResolver.createScene(engineType, HudSceneMode.RIDE, kart)
}

fun makeSpectateScene(engineType: KartEngine.Type, kart: KartRef.Specific<NitroEngine>): HudScene<NitroEngine> {
    return HudSceneResolver.createScene(engineType, HudSceneMode.SPECTATE, kart)
}
