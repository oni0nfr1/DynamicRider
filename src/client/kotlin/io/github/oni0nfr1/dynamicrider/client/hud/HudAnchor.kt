package io.github.oni0nfr1.dynamicrider.client.hud

import org.joml.Vector2i

enum class HudAnchor {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    MIDDLE_LEFT,
    MIDDLE_CENTER,
    MIDDLE_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT;

    fun point(width: Int, height: Int): Vector2i {
        return when (this) {
            TOP_LEFT -> Vector2i(0, 0)
            TOP_CENTER -> Vector2i(width / 2, 0)
            TOP_RIGHT -> Vector2i(width, 0)
            MIDDLE_LEFT -> Vector2i(0, height / 2)
            MIDDLE_CENTER -> Vector2i(width / 2, height / 2)
            MIDDLE_RIGHT -> Vector2i(width, height / 2)
            BOTTOM_LEFT -> Vector2i(0, height)
            BOTTOM_CENTER -> Vector2i(width / 2, height)
            BOTTOM_RIGHT -> Vector2i(width, height)
        }
    }
}