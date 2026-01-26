package io.github.oni0nfr1.dynamicrider.client

import net.minecraft.resources.ResourceLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ResourceStore {
    const val MOD_ID = "dynrider"
    val logger: Logger = LoggerFactory.getLogger("dynrider")

    val hudId: ResourceLocation
            = ResourceLocation.fromNamespaceAndPath(MOD_ID, "hud")
    val boosterIcon: ResourceLocation
        = ResourceLocation.fromNamespaceAndPath(MOD_ID, "textures/gui/boost_icon.png")
    val dseg_font: ResourceLocation
        = ResourceLocation.fromNamespaceAndPath(MOD_ID, "dseg7")
}