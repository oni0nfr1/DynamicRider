package io.github.oni0nfr1.dynamicrider.client

import net.minecraft.resources.ResourceLocation

object ResourceStore {
    const val MOD_ID = "dynrider"
    val boosterIcon: ResourceLocation
        = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/boost_icon.png")
}