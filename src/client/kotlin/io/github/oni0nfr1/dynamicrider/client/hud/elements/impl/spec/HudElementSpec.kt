package io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState

interface HudElementSpec<out ELEMENT : HudElement<S>, in S : KartState> {
    val layout: HudLayoutSpec

    fun requiredStateClass(): Class<out KartState>

    fun create(context: HudSceneContext<S>, parent: ElementHolder): HudElement<S>
}
