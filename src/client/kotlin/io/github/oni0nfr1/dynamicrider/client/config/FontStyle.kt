package io.github.oni0nfr1.dynamicrider.client.config

import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation

enum class FontStyle(val fontId: ResourceLocation?, val key: String) {
    VANILLA(null, "vanilla"),

    NEXON_LV2(ResourceLocation.fromNamespaceAndPath("dynrider", "nexon_lv2"), "nexon_lv2");

    val style: Style
        get() {
            val id = this.fontId ?: return Style.EMPTY
            return Style.EMPTY.withFont(id)
        }

    val translationKey: String
        get() = "dynrider.font.$key"
}