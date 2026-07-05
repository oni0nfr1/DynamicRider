package io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

interface HudElementSpec<out ELEMENT: HudElement<E>, in E: KartEngine> {
    val layout: HudLayoutSpec

    fun requiredEngineClass(): Class<out KartEngine>

    fun create(kart: KartRef.Specific<E>, parent: ElementHolder): HudElement<E>
}
