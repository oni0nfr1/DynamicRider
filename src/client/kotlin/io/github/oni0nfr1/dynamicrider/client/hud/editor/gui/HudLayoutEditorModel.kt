package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditableProperty
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

/** `HudLayoutSpec`의 복수 leaf를 한 동작으로 갱신하기 위한 값을 계산한다. */
object HudLayoutEditorModel {
    /** layout의 위치 두 축을 같은 Spec 교체에서 갱신한다. */
    fun replacePosition(layout: HudEditableProperty, x: Int, y: Int): JsonObject {
        val current = layout.value as? JsonObject
            ?: error("HUD layout property must be a JSON object")
        return JsonObject(current + mapOf("x" to JsonPrimitive(x), "y" to JsonPrimitive(y)))
    }

    /** layout의 현재 위치에 주어진 pixel offset을 더한다. */
    fun translatePosition(layout: HudEditableProperty, deltaX: Int, deltaY: Int): JsonObject {
        val current = layout.value as? JsonObject
            ?: error("HUD layout property must be a JSON object")
        val x = current.getValue("x").jsonPrimitive.int
        val y = current.getValue("y").jsonPrimitive.int
        return replacePosition(layout, x + deltaX, y + deltaY)
    }

    /** layout의 두 scale 축을 같은 값으로 갱신한다. */
    fun replaceUniformScale(layout: HudEditableProperty, scale: Float): JsonObject {
        val current = layout.value as? JsonObject
            ?: error("HUD layout property must be a JSON object")
        return JsonObject(current + mapOf("scaleX" to JsonPrimitive(scale), "scaleY" to JsonPrimitive(scale)))
    }
}
