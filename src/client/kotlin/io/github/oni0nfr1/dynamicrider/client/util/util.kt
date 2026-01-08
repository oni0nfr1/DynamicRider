package io.github.oni0nfr1.dynamicrider.client.util

import io.github.oni0nfr1.dynamicrider.client.event.Kart
import net.minecraft.world.entity.Entity

fun Int.ordinal(): String {
    val suffix = when (this % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
    return "$this$suffix"
}

fun Entity?.isKart(): Boolean {
    return this != null && this is Kart
}