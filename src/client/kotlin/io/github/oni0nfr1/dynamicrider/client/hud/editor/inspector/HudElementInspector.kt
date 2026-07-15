package io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/** Registry metadata와 document의 현재 Spec 값을 결합해 GUI용 읽기 모델을 생성한다. */
class HudElementInspector(
    private val document: HudSceneDocument,
) {
    private val json = Json {
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

        val properties = mutableListOf<HudEditableProperty>()
        for (property in type.metadata.properties) {
            if (property.hidden) continue
            val value = encoded[property.serialName]
                ?: return HudElementInspectionResult.EncodingFailed(
                    elementId = elementId,
                    specType = element.spec::class.java.name,
                    cause = IllegalStateException(
                        "Encoded HUD element '${type.id}' is missing property '${property.serialName}'"
                    ),
                )
            properties += HudEditableProperty(
                path = HudPropertyPath.of(property.serialName),
                nameKey = property.nameKey,
                descriptionKey = property.descriptionKey,
                editor = property.editor,
                value = value,
                optional = property.optional,
                nullable = property.nullable,
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
}
