package io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.dsl

import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec

@HUDSL
abstract class HudElementBuilder<out SPEC : HudElementSpec<*, *>> {
    private var layoutBuilder = HudLayoutBuilder()

    fun layout(init: HudLayoutBuilder.() -> Unit) {
        layoutBuilder = HudLayoutBuilder().apply(init)
    }

    protected inline fun <reified BUILDER, CHILD_SPEC> child(block: BUILDER.() -> Unit): CHILD_SPEC
        where
            BUILDER : HudElementBuilder<CHILD_SPEC>,
            CHILD_SPEC : HudElementSpec<*, *> {
        val builder = BUILDER::class.java.getDeclaredConstructor().newInstance()
        return builder.apply(block).build()
    }

    fun build(): SPEC = build(layoutBuilder.build())

    protected abstract fun build(layout: HudLayoutSpec): SPEC
}
