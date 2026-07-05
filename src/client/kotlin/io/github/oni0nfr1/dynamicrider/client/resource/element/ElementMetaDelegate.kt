package io.github.oni0nfr1.dynamicrider.client.resource.element

import kotlinx.serialization.DeserializationStrategy
import net.minecraft.resources.ResourceLocation
import kotlin.reflect.KProperty

class ElementMetaDelegate<T : ElementMetaData>(
    private val id: ResourceLocation,
    private val deserializer: DeserializationStrategy<T>,
) {
    operator fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T = ElementRegistry.requireElementMeta(id, deserializer)
}
