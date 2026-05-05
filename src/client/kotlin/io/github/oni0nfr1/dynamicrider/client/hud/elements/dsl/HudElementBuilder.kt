package io.github.oni0nfr1.dynamicrider.client.hud.elements.dsl

import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudLayoutSpec

@HUDSL
abstract class HudElementBuilder<out SPEC : HudElementSpec<*, *>> {
    private var layoutBuilder = HudLayoutBuilder()

    fun layout(init: HudLayoutBuilder.() -> Unit) {
        layoutBuilder = HudLayoutBuilder().apply(init)
    }

    fun build(): SPEC = build(layoutBuilder.build())

    protected abstract fun build(layout: HudLayoutSpec): SPEC
}
