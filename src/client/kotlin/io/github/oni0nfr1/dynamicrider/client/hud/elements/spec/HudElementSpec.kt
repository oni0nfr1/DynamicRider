package io.github.oni0nfr1.dynamicrider.client.hud.elements.spec

import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

abstract class HudElementSpec<out ELEMENT: HudElement<E>, in E: KartEngine> {
    abstract val layout: HudLayoutSpec

    abstract fun requiredEngineClass(): Class<out KartEngine>

    abstract fun create(kart: KartRef.Specific<E>): HudElement<E>
}
