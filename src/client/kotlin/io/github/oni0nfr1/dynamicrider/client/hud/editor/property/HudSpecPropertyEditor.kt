package io.github.oni0nfr1.dynamicrider.client.hud.editor.property

import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Failure
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Reason
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult.Success
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementType
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyMetadata
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
        ignoreUnknownKeys = true
        encodeDefaults = true
        allowSpecialFloatingPointValues = true
    }

    /**
     * [spec]의 [path]에 [value]를 넣은 새 spec을 생성한다.
     *
     * top-level primitive, enum 및 `@HudLayout` property만 지원한다. 다른 중첩 object와
     * list는 기본 편집기가 완성된 뒤 별도의 재귀 편집 모델에서 처리한다. 원본 [spec]은
     * 변경하지 않으며 실패에는 입력 [path]가 보존된다.
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
        val property = type.metadata.properties.firstOrNull { it.serialName == path.segments.first() }
            ?: return Failure(
                path,
                Reason.UNKNOWN_PROPERTY,
                "Unknown HUD property '${path.segments.first()}'",
            )

        unsupportedReason(property, path)?.let { return it }
        validatePath(property, path)?.let { return it }
        validateInput(property, path, value)?.let { return it }

        return update(type, spec, property, path, value)
    }

    private fun update(
        type: HudElementType<*, *>,
        spec: HudElementSpec<*, *>,
        property: HudPropertyMetadata,
        path: HudPropertyPath,
        value: JsonElement,
    ): HudSpecPropertyUpdateResult {
        @Suppress("UNCHECKED_CAST")
        val serializer = type.serializer as KSerializer<HudElementSpec<*, *>>
        return try {
            val encoded = json.encodeToJsonElement(serializer, spec) as? JsonObject
                ?: return Failure(path, Reason.INVALID_VALUE, "HUD element spec must serialize as an object")
            val updated = replace(encoded, path.segments, value)
                ?: return Failure(path, Reason.UNKNOWN_PROPERTY, "Unknown HUD property '$path'")

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

    private fun validatePath(
        property: HudPropertyMetadata,
        path: HudPropertyPath,
    ): Failure? {
        val isLayout = property.editor is HudPropertyEditorType.LayoutEditor
        if (!isLayout && path.segments.size > 1) {
            return Failure(
                path,
                Reason.UNSUPPORTED_PROPERTY,
                "Nested editing is only supported for @HudLayout properties",
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
            return if (property.nullable && path.segments.size == 1) null else Failure(
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
}
