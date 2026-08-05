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
    val role: HudPropertyRole = HudPropertyRole.DEFAULT,
) {
    val editor: HudPropertyEditorType
        get() = when (schema) {
            is HudPropertySchema.Value -> when (val value = schema.schema) {
                is HudValueSchema.Leaf -> value.editor
                is HudValueSchema.Object -> HudPropertyEditorType.Unsupported(value.serialName)
                is HudValueSchema.Sealed -> HudPropertyEditorType.Unsupported(value.serialName)
                is HudValueSchema.Unsupported -> HudPropertyEditorType.Unsupported(value.serialName)
            }
            is HudPropertySchema.Element -> HudPropertyEditorType.Unsupported(schema.schema.serialName)
        }
}

/** 직렬화 property가 일반 값인지 자식 HUD 요소 슬롯인지 구분한다. */
sealed interface HudPropertySchema {
    data class Value(
        val schema: HudValueSchema,
    ) : HudPropertySchema

    data class Element(
        val schema: HudElementSlotSchema,
    ) : HudPropertySchema
}

/** Inspector에서 편집하는 일반 값 property의 구조다. */
sealed interface HudValueSchema {
    data class Leaf(
        val editor: HudPropertyEditorType,
    ) : HudValueSchema

    data class Object(
        val serialName: String,
        val properties: List<HudPropertyMetadata>,
    ) : HudValueSchema

    data class Sealed(
        val serialName: String,
        val discriminator: String,
        val variants: List<HudPropertyVariantMetadata>,
    ) : HudValueSchema {
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
    ) : HudValueSchema
}

/** 데이터 스키마와 별개로 editor와 runtime 도구가 사용하는 property의 의미다. */
enum class HudPropertyRole {
    DEFAULT,
    LAYOUT,
}

/** Hierarchy에서 표현하는 자식 HUD 요소 property의 타입 선택 구조다. */
sealed interface HudElementSlotSchema {
    val serialName: String

    data class Fixed(
        override val serialName: String,
    ) : HudElementSlotSchema

    data class Sealed(
        override val serialName: String,
        val discriminator: String,
        val variants: List<HudElementVariantMetadata>,
    ) : HudElementSlotSchema {
        init {
            require(discriminator.isNotBlank()) { "Sealed element discriminator must not be blank" }
            require(variants.isNotEmpty()) { "Sealed element property '$serialName' must have variants" }
            require(variants.map(HudElementVariantMetadata::serialName).distinct().size == variants.size) {
                "Sealed element property '$serialName' has duplicate variant serial names"
            }
        }
    }
}

/** sealed property가 선택할 수 있는 단일 subtype과 그 내부 property metadata다. */
data class HudPropertyVariantMetadata(
    val serialName: String,
    val nameKey: String,
    val properties: List<HudPropertyMetadata>,
)

/** sealed 자식 요소 슬롯이 선택할 수 있는 등록된 요소 타입이다. */
data class HudElementVariantMetadata(
    val serialName: String,
    val nameKey: String,
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
