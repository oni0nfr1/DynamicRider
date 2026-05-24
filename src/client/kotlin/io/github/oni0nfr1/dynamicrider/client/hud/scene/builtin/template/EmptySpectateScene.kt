package io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.template

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.hudScene
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

internal inline fun <reified E : KartEngine> emptySpectateScene(kart: KartRef.Specific<E>): HudScene<E> =
    hudScene(kart) {}
