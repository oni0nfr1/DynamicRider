package io.github.oni0nfr1.dynamicrider.client.hud.validation

import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudRange
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/** registry serializer와 descriptor metadata를 사용해 HUD spec 전체를 재귀적으로 검증한다. */
@OptIn(ExperimentalSerializationApi::class)
object HudSpecValidator {
    private val json = Json {
        encodeDefaults = true
        allowSpecialFloatingPointValues = true
    }

    /** [spec]과 그 안에 포함된 중첩 object 및 collection 값을 검증한다. */
    fun validate(spec: HudElementSpec<*, *>): HudSpecValidationResult {
        val type = HudElementTypeRegistry.bySpec(spec)
            ?: return invalid(
                HudSpecPath(),
                HudSpecValidationErrorCode.UNREGISTERED_SPEC,
                "HUD element spec '${spec::class.qualifiedName}' is not registered",
            )

        @Suppress("UNCHECKED_CAST")
        val serializer = type.serializer as KSerializer<HudElementSpec<*, *>>
        val encoded = try {
            json.encodeToJsonElement(serializer, spec)
        } catch (exception: SerializationException) {
            return serializationFailure(exception)
        } catch (exception: IllegalArgumentException) {
            return serializationFailure(exception)
        }

        val errors = mutableListOf<HudSpecValidationError>()
        validateValue(serializer.descriptor, encoded, HudSpecPath(), errors)
        return if (errors.isEmpty()) HudSpecValidationResult.Valid else HudSpecValidationResult.Invalid(errors)
    }

    private fun validateValue(
        descriptor: SerialDescriptor,
        value: JsonElement,
        path: HudSpecPath,
        errors: MutableList<HudSpecValidationError>,
    ) {
        if (value is JsonNull) return

        when (descriptor.kind) {
            PrimitiveKind.FLOAT, PrimitiveKind.DOUBLE -> validateFinite(value, path, errors)
            StructureKind.CLASS, StructureKind.OBJECT -> validateObject(descriptor, value, path, errors)
            StructureKind.LIST -> validateList(descriptor, value, path, errors)
            StructureKind.MAP -> validateMap(descriptor, value, path, errors)
            else -> Unit
        }
    }

    private fun validateObject(
        descriptor: SerialDescriptor,
        value: JsonElement,
        path: HudSpecPath,
        errors: MutableList<HudSpecValidationError>,
    ) {
        val objectValue = value as? JsonObject ?: return
        repeat(descriptor.elementsCount) { index ->
            val name = descriptor.getElementName(index)
            val childValue = objectValue[name] ?: return@repeat
            val childPath = path.child(name)
            descriptor.getElementAnnotations(index)
                .filterIsInstance<HudRange>()
                .singleOrNull()
                ?.let { validateRange(it, childValue, childPath, errors) }
            validateValue(descriptor.getElementDescriptor(index), childValue, childPath, errors)
        }
    }

    private fun validateList(
        descriptor: SerialDescriptor,
        value: JsonElement,
        path: HudSpecPath,
        errors: MutableList<HudSpecValidationError>,
    ) {
        val arrayValue = value as? JsonArray ?: return
        val elementDescriptor = descriptor.getElementDescriptor(0)
        arrayValue.forEachIndexed { index, childValue ->
            validateValue(elementDescriptor, childValue, path.child(index.toString()), errors)
        }
    }

    private fun validateMap(
        descriptor: SerialDescriptor,
        value: JsonElement,
        path: HudSpecPath,
        errors: MutableList<HudSpecValidationError>,
    ) {
        val objectValue = value as? JsonObject ?: return
        val valueDescriptor = descriptor.getElementDescriptor(1)
        objectValue.forEach { (key, childValue) ->
            validateValue(valueDescriptor, childValue, path.child(key), errors)
        }
    }

    private fun validateFinite(
        value: JsonElement,
        path: HudSpecPath,
        errors: MutableList<HudSpecValidationError>,
    ) {
        val number = (value as? JsonPrimitive)?.doubleOrNull ?: return
        if (!number.isFinite()) {
            errors += HudSpecValidationError(
                path,
                HudSpecValidationErrorCode.NON_FINITE_NUMBER,
                "HUD property '$path' must be finite",
            )
        }
    }

    private fun validateRange(
        range: HudRange,
        value: JsonElement,
        path: HudSpecPath,
        errors: MutableList<HudSpecValidationError>,
    ) {
        val number = (value as? JsonPrimitive)?.doubleOrNull ?: return
        if (number.isFinite() && number !in range.min..range.max) {
            errors += HudSpecValidationError(
                path,
                HudSpecValidationErrorCode.OUT_OF_RANGE,
                "HUD property '$path' must be between ${range.min} and ${range.max}",
            )
        }
    }

    private fun serializationFailure(exception: Exception): HudSpecValidationResult = invalid(
        HudSpecPath(),
        HudSpecValidationErrorCode.SERIALIZATION_FAILURE,
        exception.message ?: "HUD element spec could not be serialized",
    )

    private fun invalid(
        path: HudSpecPath,
        code: HudSpecValidationErrorCode,
        message: String,
    ): HudSpecValidationResult.Invalid = HudSpecValidationResult.Invalid(
        listOf(HudSpecValidationError(path, code, message))
    )
}
