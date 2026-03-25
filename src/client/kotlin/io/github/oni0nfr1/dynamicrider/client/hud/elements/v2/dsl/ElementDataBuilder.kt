package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.impl.HudElementImpl

@HUDSL
abstract class ElementDataBuilder<SELF, TARGET>
    where
        SELF : ElementDataBuilder<SELF, TARGET>,
        TARGET: HudElementImpl<SELF, TARGET>
{
    var layout = ElementLayoutBuilder()
        private set

    fun layout(init: ElementLayoutBuilder.() -> Unit) {
        val builder = ElementLayoutBuilder()
        builder.init()
        layout = builder
    }
}