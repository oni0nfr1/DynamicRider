package io.github.oni0nfr1.dynamicrider.client.util

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.event.Kart
import io.github.oni0nfr1.dynamicrider.client.rider.Millis
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

val Millis.minutes: Int
    get() = (this / 60000).toInt()
val Millis.seconds: Int
    get() = ((this % 60000) / 1000).toInt()
val Millis.milliseconds: Int
    get() = (this % 1000).toInt()

fun debugLog(msg: String) {
    if (!DynRiderJvmFlags.devMode) return
    ResourceStore.logger.info("[DYNRIDER_DEBUG] $msg")
}

fun infoLog(msg: String) {
    ResourceStore.logger.info("[DynamicRider] $msg")
}

fun warnLog(msg: String) {
    ResourceStore.logger.warn("[DynamicRider] $msg")
}

fun colorFromRGB(r: Int, g: Int, b: Int): Int {
    return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}