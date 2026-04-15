package io.github.oni0nfr1.dynamicrider.client.hud.scenes

import io.github.oni0nfr1.dynamicrider.client.hud.scenes.v2.defaultScene
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.v2.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.v2.defaultSpectateScene
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.engine.KartEngine.Type.*

fun makeScene(engineType: KartEngine.Type): HudScene {
    return when (engineType) {
        X,
        EX,
        JIU,
        NEW,
        Z7,
        V1,
        A2,
        LEGACY,
        PRO,
        RUSHPLUS,
        CHARGE,
        N1,
        KEY,
        MK,
        BOAT,
        GEAR,
        F1,
        RALLY -> defaultScene()
    }
}

fun makeSpectateScene(engineType: KartEngine.Type): HudScene {
    return when (engineType) {
        X,
        EX,
        JIU,
        NEW,
        Z7,
        V1,
        A2,
        LEGACY,
        PRO,
        RUSHPLUS,
        CHARGE,
        N1,
        KEY,
        MK,
        BOAT,
        GEAR,
        F1,
        RALLY -> defaultSpectateScene()
    }
}
