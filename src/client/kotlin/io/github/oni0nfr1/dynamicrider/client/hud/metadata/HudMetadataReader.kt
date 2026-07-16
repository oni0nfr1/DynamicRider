package io.github.oni0nfr1.dynamicrider.client.hud.metadata

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import java.util.Locale

/** kotlinx.serialization descriptor를 HUD 편집 메타데이터로 변환한다. */
@OptIn(ExperimentalSerializationApi::class)
object HudMetadataReader {
    private val anchorSerialName = HudAnchor.serializer().descriptor.serialName

    /** [serializer]가 설명하는 요소의 편집 메타데이터를 읽는다. */
    fun read(serializer: KSerializer<*>): HudElementMetadata = read(serializer.descriptor)

    /** [descriptor]가 설명하는 요소의 편집 메타데이터를 읽는다. */
    fun read(descriptor: SerialDescriptor): HudElementMetadata {
        val elementInfo = descriptor.annotations.filterIsInstance<HudElementInfo>().singleOrNull()
        val elementId = descriptor.serialName.toTranslationId()
        val category = elementInfo?.category?.toTranslationId() ?: DEFAULT_CATEGORY
        return HudElementMetadata(
            serialName = descriptor.serialName,
            nameKey = elementInfo?.nameKey?.takeIf(String::isNotBlank)
                ?: "$ELEMENT_KEY_PREFIX.$elementId.name",
            category = category,
            categoryNameKey = "$CATEGORY_KEY_PREFIX.$category",
            icon = elementInfo?.icon?.takeIf(String::isNotBlank),
            properties = List(descriptor.elementsCount) { index ->
                readProperty(descriptor, "$ELEMENT_KEY_PREFIX.$elementId.property", index)
            },
        )
    }

    private fun readProperty(
        owner: SerialDescriptor,
        propertyKeyPrefix: String,
        index: Int,
    ): HudPropertyMetadata {
        val serialName = owner.getElementName(index)
        val descriptor = owner.getElementDescriptor(index)
        val annotations = owner.getElementAnnotations(index)
        val property = annotations.filterIsInstance<HudProperty>().singleOrNull()
        val range = annotations.filterIsInstance<HudRange>().singleOrNull()
        val color = annotations.filterIsInstance<HudColor>().singleOrNull()
        val layout = annotations.any { it is HudLayout }

        require(!(layout && (range != null || color != null))) {
            "Property '$serialName' cannot combine @HudLayout with @HudRange or @HudColor"
        }
        require(!(range != null && color != null)) {
            "Property '$serialName' cannot combine @HudRange with @HudColor"
        }

        return HudPropertyMetadata(
            index = index,
            serialName = serialName,
            nameKey = property?.nameKey?.takeIf(String::isNotBlank)
                ?: "$propertyKeyPrefix.${serialName.toTranslationId()}.name",
            descriptionKey = property?.descriptionKey?.takeIf(String::isNotBlank),
            schema = propertySchema(
                propertyName = serialName,
                descriptor = descriptor,
                range = range,
                color = color,
                layout = layout,
                nestedKeyPrefix = "$propertyKeyPrefix.${serialName.toTranslationId()}",
            ),
            optional = owner.isElementOptional(index),
            nullable = descriptor.isNullable,
            hidden = annotations.any { it is HudHidden },
        )
    }

    private fun propertySchema(
        propertyName: String,
        descriptor: SerialDescriptor,
        range: HudRange?,
        color: HudColor?,
        layout: Boolean,
        nestedKeyPrefix: String,
    ): HudPropertySchema {
        if (layout) return HudPropertySchema.Leaf(HudPropertyEditorType.LayoutEditor)
        if (color != null) return HudPropertySchema.Leaf(HudPropertyEditorType.ColorPicker(color.alpha))
        if (descriptor.serialName == anchorSerialName) {
            return HudPropertySchema.Leaf(HudPropertyEditorType.AnchorSelector)
        }

        if (descriptor.kind == PolymorphicKind.SEALED) {
            val variants = descriptor.hudSealedVariants().map { variant ->
                val variantId = variant.serialName.toTranslationId()
                val info = variant.descriptor.annotations.filterIsInstance<HudVariantInfo>().singleOrNull()
                HudPropertyVariantMetadata(
                    serialName = variant.serialName,
                    nameKey = info?.nameKey?.takeIf(String::isNotBlank)
                        ?: "$nestedKeyPrefix.variant.$variantId.name",
                    properties = List(variant.descriptor.elementsCount) { index ->
                        readProperty(
                            variant.descriptor,
                            "$nestedKeyPrefix.variant.$variantId.property",
                            index,
                        )
                    },
                )
            }
            return HudPropertySchema.Sealed(
                serialName = descriptor.serialName,
                discriminator = HUD_CLASS_DISCRIMINATOR,
                variants = variants,
            )
        }

        val numberType = descriptor.kind.toNumberType()
        if (range != null) {
            require(numberType != null) {
                "Property '$propertyName' uses @HudRange but is not a numeric primitive"
            }
            require(range.min.isFinite() && range.max.isFinite() && range.min <= range.max) {
                "Property '$propertyName' has an invalid @HudRange"
            }
            require(range.step.isFinite() && range.step >= 0.0) {
                "Property '$propertyName' has an invalid @HudRange step"
            }
            return HudPropertySchema.Leaf(HudPropertyEditorType.Slider(
                numberType = numberType,
                range = HudNumericRange(
                    min = range.min,
                    max = range.max,
                    step = range.step.takeIf { it > 0.0 },
                ),
            ))
        }

        val editor = when (descriptor.kind) {
            PrimitiveKind.BOOLEAN -> HudPropertyEditorType.BooleanToggle
            PrimitiveKind.STRING, PrimitiveKind.CHAR -> HudPropertyEditorType.StringInput
            SerialKind.ENUM -> HudPropertyEditorType.EnumSelector(
                List(descriptor.elementsCount, descriptor::getElementName),
            )
            else -> numberType?.let(HudPropertyEditorType::NumberInput)
        }
        return editor?.let(HudPropertySchema::Leaf)
            ?: HudPropertySchema.Unsupported(descriptor.serialName)
    }

    private fun SerialKind.toNumberType(): HudNumberType? = when (this) {
        PrimitiveKind.BYTE -> HudNumberType.BYTE
        PrimitiveKind.SHORT -> HudNumberType.SHORT
        PrimitiveKind.INT -> HudNumberType.INT
        PrimitiveKind.LONG -> HudNumberType.LONG
        PrimitiveKind.FLOAT -> HudNumberType.FLOAT
        PrimitiveKind.DOUBLE -> HudNumberType.DOUBLE
        else -> null
    }

    private fun String.toTranslationId(): String =
        substringAfterLast('.')
            .replace(CAMEL_CASE_BOUNDARY, "$1_$2")
            .lowercase(Locale.ROOT)
            .replace(INVALID_TRANSLATION_ID_CHAR, "_")

    private const val ELEMENT_KEY_PREFIX = "dynamicrider.hud.element"
    private const val CATEGORY_KEY_PREFIX = "dynamicrider.hud.category"
    private const val DEFAULT_CATEGORY = "other"
    private val CAMEL_CASE_BOUNDARY = Regex("([a-z0-9])([A-Z])")
    private val INVALID_TRANSLATION_ID_CHAR = Regex("[^a-z0-9_-]")
}
