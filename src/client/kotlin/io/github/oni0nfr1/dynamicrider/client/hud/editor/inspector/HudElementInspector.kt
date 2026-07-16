package io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HUD_CLASS_DISCRIMINATOR
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyMetadata
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertySchema
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
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

    private fun inspectProperties(
        metadata: List<HudPropertyMetadata>,
        encoded: JsonObject,
        parentPath: List<String>,
    ): List<HudEditableProperty> = metadata.mapNotNull { property ->
        if (property.hidden) return@mapNotNull null
        val value = encoded[property.serialName]
            ?: error("Encoded HUD property '${(parentPath + property.serialName).joinToString(".")}' is missing")
        val path = HudPropertyPath.of(*(parentPath + property.serialName).toTypedArray())
        val schema = when (val propertySchema = property.schema) {
            is HudPropertySchema.Leaf -> HudEditablePropertySchema.Leaf(propertySchema.editor)
            is HudPropertySchema.Unsupported -> HudEditablePropertySchema.Unsupported(propertySchema.serialName)
            is HudPropertySchema.Sealed -> {
                val objectValue = value as? JsonObject
                    ?: error("Encoded sealed HUD property '$path' is not an object")
                val selected = (objectValue[propertySchema.discriminator] as? JsonPrimitive)?.content
                    ?: error("Encoded sealed HUD property '$path' has no '${propertySchema.discriminator}'")
                val variant = propertySchema.variants.firstOrNull { it.serialName == selected }
                    ?: error("Encoded sealed HUD property '$path' has unknown variant '$selected'")
                HudEditablePropertySchema.Sealed(
                    serialName = propertySchema.serialName,
                    discriminator = propertySchema.discriminator,
                    selectedVariant = selected,
                    variants = propertySchema.variants.map {
                        HudEditableVariant(it.serialName, it.nameKey)
                    },
                    properties = inspectProperties(variant.properties, objectValue, path.segments),
                )
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
        )
    }
}
