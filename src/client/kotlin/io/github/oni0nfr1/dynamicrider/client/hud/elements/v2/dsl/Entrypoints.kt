package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.impl.HudElementImpl

inline fun <reified BUILDER, reified ELEMENT> element(block: BUILDER.() -> Unit): ELEMENT
    where
        BUILDER : ElementDataBuilder<BUILDER, ELEMENT>,
        ELEMENT : HudElementImpl<BUILDER, ELEMENT>
{
    val builder = BUILDER::class.java.getDeclaredConstructor().newInstance()
    builder.block()
    val element = ELEMENT::class.java.getDeclaredConstructor(BUILDER::class.java).newInstance(builder)
    return element
}