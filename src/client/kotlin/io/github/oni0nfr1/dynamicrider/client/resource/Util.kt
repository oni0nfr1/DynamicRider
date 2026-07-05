package io.github.oni0nfr1.dynamicrider.client.resource

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import net.minecraft.resources.ResourceLocation

fun elementId(id: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(
    ResourceStore.MOD_ID,
    id,
)