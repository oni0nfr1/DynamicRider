package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditableProperty
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditablePropertySchema
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudNumberType
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

/** `HudLayoutSpec`의 고정 필드를 전용 editor가 사용할 property row로 변환한다. */
object HudLayoutEditorModel {
    fun fields(layout: HudEditableProperty): List<HudEditableProperty> {
        val value = layout.value as? JsonObject ?: return emptyList()
        return FIELD_DEFINITIONS.mapNotNull { definition ->
            value[definition.serialName]?.let { fieldValue ->
                HudEditableProperty(
                    path = HudPropertyPath.of(*(layout.path.segments + definition.serialName).toTypedArray()),
                    nameKey = "dynamicrider.hud.editor.layout.${definition.translationId}",
                    descriptionKey = null,
                    schema = HudEditablePropertySchema.Leaf(definition.editor),
                    value = fieldValue,
                    optional = false,
                    nullable = false,
                )
            }
        }
    }

    fun replace(layout: HudEditableProperty, field: HudEditableProperty, value: JsonElement): JsonObject {
        val current = layout.value as? JsonObject
            ?: error("HUD layout property must be a JSON object")
        val serialName = field.path.segments.last()
        require(FIELD_DEFINITIONS.any { it.serialName == serialName }) {
            "Unknown HUD layout field '$serialName'"
        }
        return JsonObject(current + (serialName to value))
    }

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

    private data class FieldDefinition(
        val serialName: String,
        val translationId: String,
        val editor: HudPropertyEditorType,
    )

    private val FIELD_DEFINITIONS = listOf(
        FieldDefinition("screenAnchor", "screen_anchor", HudPropertyEditorType.AnchorSelector),
        FieldDefinition("elementAnchor", "element_anchor", HudPropertyEditorType.AnchorSelector),
        FieldDefinition("scaleX", "scale_x", HudPropertyEditorType.NumberInput(HudNumberType.FLOAT)),
        FieldDefinition("scaleY", "scale_y", HudPropertyEditorType.NumberInput(HudNumberType.FLOAT)),
        FieldDefinition("x", "x", HudPropertyEditorType.NumberInput(HudNumberType.INT)),
        FieldDefinition("y", "y", HudPropertyEditorType.NumberInput(HudNumberType.INT)),
        FieldDefinition("zIndex", "z_index", HudPropertyEditorType.NumberInput(HudNumberType.FLOAT)),
    )
}
