package io.github.oni0nfr1.dynamicrider.client.hud.metadata

/** 직렬화 descriptor에서 추출한 HUD 요소 편집 정보다. */
data class HudElementMetadata(
    val serialName: String,
    val nameKey: String,
    val category: String,
    val categoryNameKey: String,
    val icon: String?,
    val properties: List<HudPropertyMetadata>,
)

/** 직렬화된 단일 속성의 편집 정보다. */
data class HudPropertyMetadata(
    val index: Int,
    val serialName: String,
    val nameKey: String,
    val descriptionKey: String?,
    val schema: HudPropertySchema,
    val optional: Boolean,
    val nullable: Boolean,
    val hidden: Boolean,
) {
    val editor: HudPropertyEditorType
        get() = when (schema) {
            is HudPropertySchema.Leaf -> schema.editor
            is HudPropertySchema.Sealed -> HudPropertyEditorType.Unsupported(schema.serialName)
            is HudPropertySchema.Unsupported -> HudPropertyEditorType.Unsupported(schema.serialName)
        }
}

/** 직렬화 property의 leaf 편집기 또는 재귀 container 구조다. */
sealed interface HudPropertySchema {
    data class Leaf(
        val editor: HudPropertyEditorType,
    ) : HudPropertySchema

    data class Sealed(
        val serialName: String,
        val discriminator: String,
        val variants: List<HudPropertyVariantMetadata>,
    ) : HudPropertySchema {
        init {
            require(discriminator.isNotBlank()) { "Sealed property discriminator must not be blank" }
            require(variants.isNotEmpty()) { "Sealed property '$serialName' must have variants" }
            require(variants.map(HudPropertyVariantMetadata::serialName).distinct().size == variants.size) {
                "Sealed property '$serialName' has duplicate variant serial names"
            }
        }
    }

    data class Unsupported(
        val serialName: String,
    ) : HudPropertySchema
}

/** sealed property가 선택할 수 있는 단일 subtype과 그 내부 property metadata다. */
data class HudPropertyVariantMetadata(
    val serialName: String,
    val nameKey: String,
    val properties: List<HudPropertyMetadata>,
)

/** 숫자 입력에서 보존해야 하는 primitive 타입이다. */
enum class HudNumberType {
    BYTE,
    SHORT,
    INT,
    LONG,
    FLOAT,
    DOUBLE,
}

data class HudNumericRange(
    val min: Double,
    val max: Double,
    val step: Double?,
)

/** 속성 패널이 선택할 입력 방식이다. */
sealed interface HudPropertyEditorType {
    data object BooleanToggle : HudPropertyEditorType
    data object StringInput : HudPropertyEditorType
    data object AnchorSelector : HudPropertyEditorType
    data object LayoutEditor : HudPropertyEditorType

    data class NumberInput(
        val numberType: HudNumberType,
    ) : HudPropertyEditorType

    data class Slider(
        val numberType: HudNumberType,
        val range: HudNumericRange,
    ) : HudPropertyEditorType

    data class EnumSelector(
        val entries: List<String>,
    ) : HudPropertyEditorType

    data class ColorPicker(
        val alpha: Boolean,
    ) : HudPropertyEditorType

    data class Unsupported(
        val serialName: String,
    ) : HudPropertyEditorType
}
