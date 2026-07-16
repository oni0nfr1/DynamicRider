package io.github.oni0nfr1.dynamicrider.client.hud.editor.property

import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Failure
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Reason
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Success
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementType
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyMetadata
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertySchema
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HUD_CLASS_DISCRIMINATOR
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidationResult
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidator
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** serializer round-trip으로 immutable HUD element spec의 단일 property를 변경한다. */
object HudSpecPropertyEditor {
    private val json = Json {
        classDiscriminator = HUD_CLASS_DISCRIMINATOR
        ignoreUnknownKeys = true
        encodeDefaults = true
        allowSpecialFloatingPointValues = true
    }

    /**
     * [spec]의 [path]에 [value]를 넣은 새 spec을 생성한다.
     *
     * top-level leaf와 현재 sealed subtype의 중첩 leaf를 지원한다. 원본 [spec]은 변경하지
     * 않으며 실패에는 입력 [path]가 보존된다.
     */
    fun update(
        spec: HudElementSpec<*, *>,
        path: HudPropertyPath,
        value: JsonElement,
    ): HudSpecPropertyUpdateResult {
        val type = HudElementTypeRegistry.bySpec(spec)
            ?: return Failure(
                path,
                Reason.UNREGISTERED_SPEC,
                "HUD element spec '${spec::class.qualifiedName}' is not registered",
            )
        val serializer = serializer(type)
        val encoded = encode(serializer, spec) ?: return Failure(
            path,
            Reason.INVALID_VALUE,
            "HUD element spec must serialize as an object",
        )
        val property = resolveProperty(type.metadata.properties, encoded, path.segments)
            ?: return Failure(path, Reason.UNKNOWN_PROPERTY, "Unknown HUD property '$path'")

        unsupportedReason(property, path)?.let { return it }
        validateInput(property, path, value)?.let { return it }

        val updated = replace(encoded, path.segments, value)
            ?: return Failure(path, Reason.UNKNOWN_PROPERTY, "Unknown HUD property '$path'")
        return decodeAndValidate(serializer, updated, path)
    }

    /** [path]의 sealed property를 [variantSerialName] subtype의 기본값으로 교체한다. */
    fun changeVariant(
        spec: HudElementSpec<*, *>,
        path: HudPropertyPath,
        variantSerialName: String,
    ): HudSpecPropertyUpdateResult {
        val type = HudElementTypeRegistry.bySpec(spec)
            ?: return Failure(path, Reason.UNREGISTERED_SPEC, "HUD element spec '${spec::class.qualifiedName}' is not registered")
        val serializer = serializer(type)
        val encoded = encode(serializer, spec)
            ?: return Failure(path, Reason.INVALID_VALUE, "HUD element spec must serialize as an object")
        val property = resolveProperty(type.metadata.properties, encoded, path.segments)
            ?: return Failure(path, Reason.UNKNOWN_PROPERTY, "Unknown HUD property '$path'")
        if (property.hidden) {
            return Failure(path, Reason.UNSUPPORTED_PROPERTY, "HUD property '${property.serialName}' is hidden")
        }
        val schema = property.schema as? HudPropertySchema.Sealed
            ?: return Failure(path, Reason.UNSUPPORTED_PROPERTY, "HUD property '$path' is not sealed")
        if (schema.variants.none { it.serialName == variantSerialName }) {
            return Failure(path, Reason.INVALID_VALUE, "Unknown variant '$variantSerialName' for HUD property '$path'")
        }
        val currentValue = valueAt(encoded, path.segments) as? JsonObject
            ?: return Failure(path, Reason.INVALID_VALUE, "HUD property '$path' is not an object")
        if ((currentValue[schema.discriminator] as? JsonPrimitive)?.content == variantSerialName) {
            return Success(spec)
        }
        val replacement = JsonObject(
            mapOf(schema.discriminator to JsonPrimitive(variantSerialName))
        )
        val updated = replace(encoded, path.segments, replacement)
            ?: return Failure(path, Reason.UNKNOWN_PROPERTY, "Unknown HUD property '$path'")
        return decodeAndValidate(serializer, updated, path)
    }

    private fun decodeAndValidate(
        serializer: KSerializer<HudElementSpec<*, *>>,
        updated: JsonObject,
        path: HudPropertyPath,
    ): HudSpecPropertyUpdateResult {
        return try {
            val updatedSpec = json.decodeFromJsonElement(serializer, updated)
            when (val validation = HudSpecValidator.validate(updatedSpec)) {
                HudSpecValidationResult.Valid -> Success(updatedSpec)
                is HudSpecValidationResult.Invalid -> {
                    val error = validation.errors.first()
                    Failure(
                        error.path.segments.takeIf { it.isNotEmpty() }
                            ?.let { HudPropertyPath.of(*it.toTypedArray()) }
                            ?: path,
                        Reason.INVALID_VALUE,
                        error.message,
                    )
                }
            }
        } catch (exception: SerializationException) {
            Failure(
                path,
                Reason.INVALID_VALUE,
                exception.message ?: "The value is not valid for HUD property '$path'",
            )
        } catch (exception: IllegalArgumentException) {
            Failure(
                path,
                Reason.INVALID_VALUE,
                exception.message ?: "The value violates an invariant of HUD property '$path'",
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun serializer(type: HudElementType<*, *>): KSerializer<HudElementSpec<*, *>> =
        type.serializer as KSerializer<HudElementSpec<*, *>>

    private fun encode(
        serializer: KSerializer<HudElementSpec<*, *>>,
        spec: HudElementSpec<*, *>,
    ): JsonObject? = try {
        json.encodeToJsonElement(serializer, spec) as? JsonObject
    } catch (exception: SerializationException) {
        null
    } catch (exception: IllegalArgumentException) {
        null
    }

    private fun unsupportedReason(
        property: HudPropertyMetadata,
        path: HudPropertyPath,
    ): Failure? {
        if (property.hidden) {
            return Failure(path, Reason.UNSUPPORTED_PROPERTY, "HUD property '${property.serialName}' is hidden")
        }
        val editor = property.editor
        if (editor is HudPropertyEditorType.Unsupported) {
            return Failure(
                path,
                Reason.UNSUPPORTED_PROPERTY,
                "HUD property '${property.serialName}' has unsupported type '${editor.serialName}'",
            )
        }
        return null
    }

    private fun validateInput(
        property: HudPropertyMetadata,
        path: HudPropertyPath,
        value: JsonElement,
    ): Failure? {
        if (value is JsonNull) {
            return if (property.nullable) null else Failure(
                path,
                Reason.INVALID_VALUE,
                "HUD property '$path' is not nullable",
            )
        }
        if (property.editor !is HudPropertyEditorType.LayoutEditor && value !is JsonPrimitive) {
            return Failure(
                path,
                Reason.INVALID_VALUE,
                "HUD property '$path' requires a primitive value",
            )
        }
        return null
    }

    private fun resolveProperty(
        properties: List<HudPropertyMetadata>,
        encoded: JsonObject,
        path: List<String>,
    ): HudPropertyMetadata? {
        val property = properties.firstOrNull { it.serialName == path.first() } ?: return null
        if (path.size == 1 || property.editor is HudPropertyEditorType.LayoutEditor) return property
        val schema = property.schema as? HudPropertySchema.Sealed ?: return null
        val objectValue = encoded[property.serialName] as? JsonObject ?: return null
        val selected = (objectValue[schema.discriminator] as? JsonPrimitive)?.content ?: return null
        val variant = schema.variants.firstOrNull { it.serialName == selected } ?: return null
        return resolveProperty(variant.properties, objectValue, path.drop(1))
    }

    private fun replace(
        objectValue: JsonObject,
        path: List<String>,
        replacement: JsonElement,
    ): JsonObject? {
        val key = path.first()
        val current = objectValue[key] ?: return null
        val newValue = if (path.size == 1) {
            replacement
        } else {
            val child = current as? JsonObject ?: return null
            replace(child, path.drop(1), replacement) ?: return null
        }
        return JsonObject(objectValue + (key to newValue))
    }

    private fun valueAt(objectValue: JsonObject, path: List<String>): JsonElement? {
        val value = objectValue[path.first()] ?: return null
        if (path.size == 1) return value
        return valueAt(value as? JsonObject ?: return null, path.drop(1))
    }
}
