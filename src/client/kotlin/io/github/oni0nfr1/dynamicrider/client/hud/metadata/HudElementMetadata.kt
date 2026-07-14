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
    val editor: HudPropertyEditorType,
    val optional: Boolean,
    val nullable: Boolean,
    val hidden: Boolean,
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
