package io.github.oni0nfr1.dynamicrider.client.util

import io.github.oni0nfr1.dynamicrider.client.event.Kart
import io.github.oni0nfr1.dynamicrider.client.rider.Millis
import net.minecraft.world.entity.Entity
import org.slf4j.Logger

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

fun Logger.debugInfo(msg: String) {
    if (!DynRiderJvmFlags.devMode) return
    this.info(msg)
}