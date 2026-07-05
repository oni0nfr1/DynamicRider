package io.github.oni0nfr1.dynamicrider.client.resource.element.data

import kotlinx.serialization.Serializable

@Serializable
data class GaugeFillRegion(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val direction: Direction,
) {
    @Serializable
    enum class Direction {
        LEFT_TO_RIGHT,
        RIGHT_TO_LEFT,
        TOP_TO_BOTTOM,
        BOTTOM_TO_TOP,
    }
}
