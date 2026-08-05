package io.github.oni0nfr1.dynamicrider.client.hud.editor.property

import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Failure
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Reason
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Success
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementType
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudElementSlotSchema
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyMetadata
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertySchema
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudValueSchema
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
        path: HudPath,
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

    /** 하나 이상의 primitive leaf 변경을 원자적으로 적용한 새 최상위 spec을 생성한다. */
    fun updateLeaf(
        spec: HudElementSpec<*, *>,
        vararg changes: Pair<HudPath, JsonPrimitive>,
    ): HudSpecPropertyUpdateResult {
        require(changes.isNotEmpty()) { "At least one HUD leaf change is required" }
        require(changes.map(Pair<HudPath, JsonPrimitive>::first).distinct().size == changes.size) {
            "HUD leaf change paths must be unique"
        }
        val type = HudElementTypeRegistry.bySpec(spec)
            ?: return Failure(
                changes.first().first,
                Reason.UNREGISTERED_SPEC,
                "HUD element spec '${spec::class.qualifiedName}' is not registered",
            )
        val serializer = serializer(type)
        val encoded = encode(serializer, spec) ?: return Failure(
            changes.first().first,
            Reason.INVALID_VALUE,
            "HUD element spec must serialize as an object",
        )

        changes.forEach { (path, value) ->
            val property = resolveProperty(type.metadata.properties, encoded, path.segments)
                ?: return Failure(path, Reason.UNKNOWN_PROPERTY, "Unknown HUD property '$path'")
            unsupportedReason(property, path)?.let { return it }
            val leaf = (property.schema as? HudPropertySchema.Value)?.schema as? HudValueSchema.Leaf
                ?: return Failure(path, Reason.UNSUPPORTED_PROPERTY, "HUD property '$path' is not a leaf")
            if (leaf.editor is HudPropertyEditorType.Unsupported) {
                return Failure(path, Reason.UNSUPPORTED_PROPERTY, "HUD property '$path' is unsupported")
            }
            validateInput(property, path, value)?.let { return it }
        }

        val updated = changes.fold(encoded) { current, (path, value) ->
            replace(current, path.segments, value)
                ?: return Failure(path, Reason.UNKNOWN_PROPERTY, "Unknown HUD property '$path'")
        }
        return decodeAndValidate(serializer, updated, changes.first().first)
    }

    /** [path]의 sealed property를 [variantSerialName] subtype의 기본값으로 교체한다. */
    fun changeVariant(
        spec: HudElementSpec<*, *>,
        path: HudPath,
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
        val currentValue = valueAt(encoded, path.segments)
            ?: return Failure(path, Reason.UNKNOWN_PROPERTY, "Unknown HUD property '$path'")
        val replacement = when (val propertySchema = property.schema) {
            is HudPropertySchema.Value -> {
                val schema = propertySchema.schema as? HudValueSchema.Sealed
                    ?: return Failure(path, Reason.UNSUPPORTED_PROPERTY, "HUD property '$path' is not sealed")
                if (schema.variants.none { it.serialName == variantSerialName }) {
                    return Failure(path, Reason.INVALID_VALUE, "Unknown variant '$variantSerialName' for HUD property '$path'")
                }
                if ((currentValue as? JsonObject)?.selectedVariant(schema.discriminator) == variantSerialName) {
                    return Success(spec)
                }
                JsonObject(mapOf(schema.discriminator to JsonPrimitive(variantSerialName)))
            }
            is HudPropertySchema.Element -> {
                val schema = propertySchema.schema as? HudElementSlotSchema.Sealed
                    ?: return Failure(path, Reason.UNSUPPORTED_PROPERTY, "HUD element property '$path' is not sealed")
                if (schema.variants.none { it.serialName == variantSerialName }) {
                    return Failure(path, Reason.INVALID_VALUE, "Unknown variant '$variantSerialName' for HUD element property '$path'")
                }
                if ((currentValue as? JsonObject)?.selectedVariant(schema.discriminator) == variantSerialName) {
                    return Success(spec)
                }
                val type = HudElementTypeRegistry.byId(variantSerialName)
                    ?: return Failure(path, Reason.INVALID_VALUE, "HUD element variant '$variantSerialName' is not registered")
                JsonObject(
                    encodeDefaultSpec(type) + (schema.discriminator to JsonPrimitive(variantSerialName)),
                )
            }
        }
        val updated = replace(encoded, path.segments, replacement)
            ?: return Failure(path, Reason.UNKNOWN_PROPERTY, "Unknown HUD property '$path'")
        return decodeAndValidate(serializer, updated, path)
    }

    /** nullable property를 비활성화하거나 스키마의 기본값으로 다시 활성화한다. */
    fun setPresence(
        spec: HudElementSpec<*, *>,
        path: HudPath,
        present: Boolean,
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
        if (!property.nullable) {
            return Failure(path, Reason.INVALID_VALUE, "HUD property '$path' is not nullable")
        }
        val currentValue = valueAt(encoded, path.segments)
            ?: return Failure(path, Reason.UNKNOWN_PROPERTY, "Unknown HUD property '$path'")
        if ((currentValue !is JsonNull) == present) return Success(spec)

        val replacement = if (!present) {
            JsonNull
        } else {
            defaultValue(property) ?: return Failure(
                path,
                Reason.UNSUPPORTED_PROPERTY,
                "HUD property '$path' has no constructible default value",
            )
        }
        val updated = replace(encoded, path.segments, replacement)
            ?: return Failure(path, Reason.UNKNOWN_PROPERTY, "Unknown HUD property '$path'")
        return decodeAndValidate(serializer, updated, path)
    }

    private fun defaultValue(property: HudPropertyMetadata): JsonElement? =
        when (val propertySchema = property.schema) {
            is HudPropertySchema.Value -> when (val schema = propertySchema.schema) {
                is HudValueSchema.Object -> JsonObject(emptyMap())
                is HudValueSchema.Sealed -> JsonObject(
                    mapOf(schema.discriminator to JsonPrimitive(schema.variants.first().serialName)),
                )
                is HudValueSchema.Leaf,
                is HudValueSchema.Unsupported,
                -> null
            }
            is HudPropertySchema.Element -> when (val schema = propertySchema.schema) {
                is HudElementSlotSchema.Fixed -> HudElementTypeRegistry.byId(schema.serialName)
                    ?.let(::encodeDefaultSpec)
                is HudElementSlotSchema.Sealed -> {
                    val variant = schema.variants.first()
                    val type = HudElementTypeRegistry.byId(variant.serialName) ?: return null
                    JsonObject(
                        encodeDefaultSpec(type) +
                            (schema.discriminator to JsonPrimitive(variant.serialName)),
                    )
                }
            }
        }

    private fun encodeDefaultSpec(type: HudElementType<*, *>): JsonObject {
        val defaultSpec = type.createDefaultSpec()
        return encode(serializer(type), defaultSpec)
            ?: error("Default HUD element spec '${type.id}' must serialize as an object")
    }

    private fun JsonObject.selectedVariant(discriminator: String): String? =
        (this[discriminator] as? JsonPrimitive)?.content

    private fun decodeAndValidate(
        serializer: KSerializer<HudElementSpec<*, *>>,
        updated: JsonObject,
        path: HudPath,
    ): HudSpecPropertyUpdateResult {
        return try {
            val updatedSpec = json.decodeFromJsonElement(serializer, updated)
            when (val validation = HudSpecValidator.validate(updatedSpec)) {
                HudSpecValidationResult.Valid -> Success(updatedSpec)
                is HudSpecValidationResult.Invalid -> {
                    val error = validation.errors.first()
                    Failure(
                        error.path.segments.takeIf { it.isNotEmpty() }
                            ?.let { HudPath.of(*it.toTypedArray()) }
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
        path: HudPath,
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
        path: HudPath,
        value: JsonElement,
    ): Failure? {
        if (value is JsonNull) {
            return if (property.nullable) null else Failure(
                path,
                Reason.INVALID_VALUE,
                "HUD property '$path' is not nullable",
            )
        }
        if (value !is JsonPrimitive) {
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
        if (path.size == 1) return property
        val objectValue = encoded[property.serialName] as? JsonObject ?: return null
        val nestedProperties = when (val schema = property.schema) {
            is HudPropertySchema.Value -> when (val valueSchema = schema.schema) {
                is HudValueSchema.Object -> valueSchema.properties
                is HudValueSchema.Sealed -> {
                    val selected = (objectValue[valueSchema.discriminator] as? JsonPrimitive)?.content
                        ?: return null
                    valueSchema.variants.firstOrNull { it.serialName == selected }?.properties
                        ?: return null
                }
                else -> return null
            }
            is HudPropertySchema.Element -> when (val elementSchema = schema.schema) {
                is HudElementSlotSchema.Fixed -> HudElementTypeRegistry.byId(elementSchema.serialName)
                    ?.metadata
                    ?.properties
                    ?: return null
                is HudElementSlotSchema.Sealed -> {
                    val selected = (objectValue[elementSchema.discriminator] as? JsonPrimitive)?.content
                        ?: return null
                    HudElementTypeRegistry.byId(selected)?.metadata?.properties ?: return null
                }
            }
        }
        return resolveProperty(nestedProperties, objectValue, path.drop(1))
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
