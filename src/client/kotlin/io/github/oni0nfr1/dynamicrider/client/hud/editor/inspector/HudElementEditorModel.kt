package io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector

import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
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
)

/** 속성 패널이 widget을 구성하는 데 필요한 metadata와 현재 직렬화 값이다. */
data class HudEditableProperty(
    val path: HudPropertyPath,
    val nameKey: String,
    val descriptionKey: String?,
    val editor: HudPropertyEditorType,
    val value: JsonElement,
    val optional: Boolean,
    val nullable: Boolean,
) {
    val supported: Boolean
        get() = editor !is HudPropertyEditorType.Unsupported
}

/** 현재 상태 타입의 요소 팔레트에 표시할 정적 요소 정보다. */
data class HudElementPaletteEntry(
    val typeId: String,
    val nameKey: String,
    val category: String,
    val categoryNameKey: String,
    val icon: String?,
)
