package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec

@HUDSL
abstract class HudElementBuilder<SPEC : HudElementSpec<*>> {
    private var layoutBuilder = HudLayoutBuilder()

    fun layout(init: HudLayoutBuilder.() -> Unit) {
        layoutBuilder = HudLayoutBuilder().apply(init)
    }

    fun build(): SPEC = build(layoutBuilder.build())

    protected abstract fun build(layout: HudLayoutSpec): SPEC
}
