package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditableProperty
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudNumberType
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

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
                    editor = definition.editor,
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
