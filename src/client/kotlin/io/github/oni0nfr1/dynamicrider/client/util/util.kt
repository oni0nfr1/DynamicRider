package io.github.oni0nfr1.dynamicrider.client.util

fun Int.ordinal(): String {
    val suffix = when (this % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
    return "$this$suffix"
}