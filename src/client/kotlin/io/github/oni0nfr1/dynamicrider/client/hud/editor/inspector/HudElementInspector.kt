package io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HUD_CLASS_DISCRIMINATOR
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyMetadata
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertySchema
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudValueSchema
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudElementSlotSchema
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/** Registry metadata와 document의 현재 Spec 값을 결합해 GUI용 읽기 모델을 생성한다. */
class HudElementInspector(
    private val document: HudSceneDocument,
) {
    private val json = Json {
        classDiscriminator = HUD_CLASS_DISCRIMINATOR
        encodeDefaults = true
        allowSpecialFloatingPointValues = true
    }

    /** [elementId] 요소의 숨겨지지 않은 property metadata와 현재 값을 반환한다. */
    fun inspect(elementId: String): HudElementInspectionResult {
        val element = document.elementById(elementId)
            ?: return HudElementInspectionResult.ElementNotFound(elementId)
        val type = HudElementTypeRegistry.bySpec(element.spec)
            ?: return HudElementInspectionResult.UnregisteredSpec(
                elementId = elementId,
                specType = element.spec::class.java.name,
            )

        @Suppress("UNCHECKED_CAST")
        val serializer = type.serializer as KSerializer<HudElementSpec<*, *>>
        val encoded = try {
            json.encodeToJsonElement(serializer, element.spec) as? JsonObject
                ?: error("HUD element spec must serialize as an object")
        } catch (cause: Exception) {
            return HudElementInspectionResult.EncodingFailed(
                elementId = elementId,
                specType = element.spec::class.java.name,
                cause = cause,
            )
        }

        val properties = try {
            inspectProperties(type.metadata.properties, encoded, emptyList())
        } catch (cause: IllegalStateException) {
            return HudElementInspectionResult.EncodingFailed(
                elementId = elementId,
                specType = element.spec::class.java.name,
                cause = cause,
            )
        }

        val metadata = type.metadata
        return HudElementInspectionResult.Inspected(
            HudElementEditorModel(
                elementId = elementId,
                typeId = type.id,
                nameKey = metadata.nameKey,
                category = metadata.category,
                categoryNameKey = metadata.categoryNameKey,
                icon = metadata.icon,
                properties = properties,
            )
        )
    }

    /** 절대 [path]의 root 또는 중첩 요소를 읽고 property 경로도 document 절대 경로로 반환한다. */
    fun inspect(path: HudPath): HudElementInspectionResult {
        val rootId = path.firstSegment
        val element = document.elementById(rootId)
            ?: return HudElementInspectionResult.ElementNotFound(rootId)
        var type = HudElementTypeRegistry.bySpec(element.spec)
            ?: return HudElementInspectionResult.UnregisteredSpec(rootId, element.spec::class.java.name)

        @Suppress("UNCHECKED_CAST")
        val rootSerializer = type.serializer as KSerializer<HudElementSpec<*, *>>
        var encoded = try {
            json.encodeToJsonElement(rootSerializer, element.spec) as? JsonObject
                ?: error("HUD element spec must serialize as an object")
        } catch (cause: Exception) {
            return HudElementInspectionResult.EncodingFailed(rootId, element.spec::class.java.name, cause)
        }
        var nameKey = type.metadata.nameKey

        try {
            path.segments.drop(1).forEachIndexed { index, segment ->
                val property = type.metadata.properties.firstOrNull { it.serialName == segment }
                    ?: error("HUD element property '${path.segments.take(index + 2).joinToString(".")}' is missing")
                val slot = (property.schema as? HudPropertySchema.Element)?.schema
                    ?: error("HUD path '${path.segments.take(index + 2).joinToString(".")}' is not an element")
                val value = encoded[segment]
                    ?: error("Encoded HUD element property '${path.segments.take(index + 2).joinToString(".")}' is missing")
                val typeId = when (slot) {
                    is HudElementSlotSchema.Fixed -> slot.serialName
                    is HudElementSlotSchema.Sealed -> (value as? JsonObject)
                        ?.get(slot.discriminator)
                        ?.let { it as? JsonPrimitive }
                        ?.content
                }
                nameKey = property.nameKey
                if (value is JsonNull) {
                    return HudElementInspectionResult.AbsentElement(path, nameKey, typeId)
                }
                type = typeId?.let(HudElementTypeRegistry::byId)
                    ?: error("HUD element type at '${path.segments.take(index + 2).joinToString(".")}' is not registered")
                encoded = value as? JsonObject
                    ?: error("Encoded HUD element property '${path.segments.take(index + 2).joinToString(".")}' is not an object")
            }

            val properties = inspectProperties(type.metadata.properties, encoded, path.segments)
            val metadata = type.metadata
            return HudElementInspectionResult.Inspected(
                HudElementEditorModel(
                    elementId = rootId,
                    typeId = type.id,
                    nameKey = nameKey,
                    category = metadata.category,
                    categoryNameKey = metadata.categoryNameKey,
                    icon = metadata.icon,
                    properties = properties,
                    path = path,
                )
            )
        } catch (cause: Exception) {
            return HudElementInspectionResult.EncodingFailed(rootId, element.spec::class.java.name, cause)
        }
    }

    private fun inspectProperties(
        metadata: List<HudPropertyMetadata>,
        encoded: JsonObject,
        parentPath: List<String>,
    ): List<HudEditableProperty> = metadata.mapNotNull { property ->
        if (property.hidden) return@mapNotNull null
        if (property.schema is HudPropertySchema.Element) return@mapNotNull null
        val value = encoded[property.serialName]
            ?: error("Encoded HUD property '${(parentPath + property.serialName).joinToString(".")}' is missing")
        val path = HudPath.of(*(parentPath + property.serialName).toTypedArray())
        val schema = when (val propertySchema = property.schema) {
            is HudPropertySchema.Element -> error("HUD element properties are owned by the hierarchy")
            is HudPropertySchema.Value -> when (val valueSchema = propertySchema.schema) {
                is HudValueSchema.Leaf -> HudEditablePropertySchema.Leaf(valueSchema.editor)
                is HudValueSchema.Object -> {
                    val objectValue = value as? JsonObject
                    HudEditablePropertySchema.Object(
                        serialName = valueSchema.serialName,
                        properties = objectValue?.let {
                            inspectProperties(valueSchema.properties, it, path.segments)
                        }.orEmpty(),
                    )
                }
                is HudValueSchema.Unsupported -> HudEditablePropertySchema.Unsupported(valueSchema.serialName)
                is HudValueSchema.Sealed -> {
                    val objectValue = value as? JsonObject
                    val selected = objectValue?.let {
                        (it[valueSchema.discriminator] as? JsonPrimitive)?.content
                            ?: error("Encoded sealed HUD property '$path' has no '${valueSchema.discriminator}'")
                    }
                    val variant = selected?.let { selectedVariant ->
                        valueSchema.variants.firstOrNull { it.serialName == selectedVariant }
                            ?: error("Encoded sealed HUD property '$path' has unknown variant '$selectedVariant'")
                    }
                    HudEditablePropertySchema.Sealed(
                        serialName = valueSchema.serialName,
                        discriminator = valueSchema.discriminator,
                        selectedVariant = selected,
                        variants = valueSchema.variants.map {
                            HudEditableVariant(it.serialName, it.nameKey)
                        },
                        properties = if (variant != null) {
                            inspectProperties(variant.properties, checkNotNull(objectValue), path.segments)
                        } else {
                            emptyList()
                        },
                    )
                }
            }
        }
        HudEditableProperty(
            path = path,
            nameKey = property.nameKey,
            descriptionKey = property.descriptionKey,
            schema = schema,
            value = value,
            optional = property.optional,
            nullable = property.nullable,
            role = property.role,
        )
    }
}
