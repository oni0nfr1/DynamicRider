package io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector

import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyRole
import kotlinx.serialization.json.JsonElement

/** 선택된 document 요소의 표시 정보와 현재 편집 가능한 property snapshot이다. */
data class HudElementEditorModel(
    val elementId: String,
    val typeId: String,
    val nameKey: String,
    val category: String,
    val categoryNameKey: String,
    val icon: String?,
    val properties: List<HudEditableProperty>,
    /** 이 모델이 설명하는 root 또는 중첩 요소의 document 절대 경로다. */
    val path: HudPath = HudPath.of(elementId),
)

/** 속성 패널이 widget을 구성하는 데 필요한 metadata와 현재 직렬화 값이다. */
data class HudEditableProperty(
    val path: HudPath,
    val nameKey: String,
    val descriptionKey: String?,
    val schema: HudEditablePropertySchema,
    val value: JsonElement,
    val optional: Boolean,
    val nullable: Boolean,
    val role: HudPropertyRole = HudPropertyRole.DEFAULT,
) {
    val editor: HudPropertyEditorType
        get() = when (schema) {
            is HudEditablePropertySchema.Leaf -> schema.editor
            is HudEditablePropertySchema.Object -> HudPropertyEditorType.Unsupported(schema.serialName)
            is HudEditablePropertySchema.Sealed -> HudPropertyEditorType.Unsupported(schema.serialName)
            is HudEditablePropertySchema.Unsupported -> HudPropertyEditorType.Unsupported(schema.serialName)
        }

    val supported: Boolean
        get() = schema !is HudEditablePropertySchema.Unsupported
}

sealed interface HudEditablePropertySchema {
    data class Leaf(
        val editor: HudPropertyEditorType,
    ) : HudEditablePropertySchema

    data class Object(
        val serialName: String,
        val properties: List<HudEditableProperty>,
    ) : HudEditablePropertySchema

    data class Sealed(
        val serialName: String,
        val discriminator: String,
        val selectedVariant: String?,
        val variants: List<HudEditableVariant>,
        val properties: List<HudEditableProperty>,
    ) : HudEditablePropertySchema

    data class Unsupported(
        val serialName: String,
    ) : HudEditablePropertySchema
}

data class HudEditableVariant(
    val serialName: String,
    val nameKey: String,
)

/** 이 모델 안에서 중첩 경로에 해당하는 현재 property를 찾는다. */
fun HudElementEditorModel.propertyAt(path: HudPath): HudEditableProperty? {
    fun find(properties: List<HudEditableProperty>): HudEditableProperty? {
        properties.forEach { property ->
            if (property.path == path) return property
            val nested = when (val schema = property.schema) {
                is HudEditablePropertySchema.Object -> schema.properties
                is HudEditablePropertySchema.Sealed -> schema.properties
                else -> return@forEach
            }
            find(nested)?.let { return it }
        }
        return null
    }
    return find(properties)
}

/** 현재 상태 타입의 요소 팔레트에 표시할 정적 요소 정보다. */
data class HudElementPaletteEntry(
    val typeId: String,
    val nameKey: String,
    val category: String,
    val categoryNameKey: String,
    val icon: String?,
)
