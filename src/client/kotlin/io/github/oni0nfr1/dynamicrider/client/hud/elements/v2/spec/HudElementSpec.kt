package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec

import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement

abstract class HudElementSpec<out ELEMENT : HudElement> {
    abstract val layout: HudLayoutSpec

    abstract fun create(): ELEMENT
}
