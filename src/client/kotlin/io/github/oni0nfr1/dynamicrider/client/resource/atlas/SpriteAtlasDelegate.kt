package io.github.oni0nfr1.dynamicrider.client.resource.atlas

import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KProperty

class SpriteAtlasDelegate(
    private val id: ResourceLocation,
) {
    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ) = AtlasRegistry.requireSprite(id)
}