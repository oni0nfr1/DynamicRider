package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditableProperty
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudNumberType
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.CycleButton
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component

/** Inspector metadata를 vanilla GUI 입력 widget으로 변환한다. */
class HudPropertyWidgetFactory(
    private val font: Font,
) {
    fun create(
        property: HudEditableProperty,
        x: Int,
        y: Int,
        width: Int,
        onCommit: (JsonElement) -> Boolean,
        onInvalidInput: (String) -> Unit,
        onCancelInput: () -> Unit = {},
        onOpenLayout: () -> Unit = {},
    ): List<AbstractWidget> = when (val editor = property.editor) {
        HudPropertyEditorType.BooleanToggle -> if (property.nullable) {
            selector(
                property = property,
                entries = listOf(NULL_LITERAL, "true", "false"),
                x = x,
                y = y,
                width = width,
                onCommit = { value -> onCommit(if (value.jsonPrimitive.content == NULL_LITERAL) JsonNull else JsonPrimitive(value.jsonPrimitive.content.toBoolean())) },
            )
        } else {
            listOf(
                CycleButton.onOffBuilder(property.value.jsonPrimitive.booleanOrNull ?: false)
                    .displayOnlyValue()
                    .create(x, y, width, WIDGET_HEIGHT, Component.translatable(property.nameKey)) { _, value ->
                        onCommit(JsonPrimitive(value))
                    }
            )
        }
        HudPropertyEditorType.StringInput -> textInput(
            property = property,
            x = x,
            y = y,
            width = width,
            parser = { value -> nullableValue(property, value) { JsonPrimitive(it) } },
            onCommit = { onCommit(it) },
            onInvalidInput = onInvalidInput,
        )
        HudPropertyEditorType.AnchorSelector -> selector(
            property = property,
            entries = HudAnchor.entries.map(HudAnchor::name),
            x = x,
            y = y,
            width = width,
            onCommit = { onCommit(it) },
        )
        is HudPropertyEditorType.EnumSelector -> selector(
            property = property,
            entries = editor.entries,
            x = x,
            y = y,
            width = width,
            onCommit = { onCommit(it) },
        )
        is HudPropertyEditorType.NumberInput -> numericInput(
            property,
            editor.numberType,
            x,
            y,
            width,
            onCommit,
            onInvalidInput,
            onCancelInput,
        )
        is HudPropertyEditorType.Slider -> if (property.nullable && property.value === JsonNull) {
            numericInput(
                property,
                editor.numberType,
                x,
                y,
                width,
                onCommit,
                onInvalidInput,
                onCancelInput,
            )
        } else {
            listOf(
                HudRangeSlider(
                    x = x,
                    y = y,
                    width = width,
                    initialValue = property.value.jsonPrimitive.content.toDouble(),
                    numberType = editor.numberType,
                    range = editor.range,
                    onCommit = { onCommit(it) },
                )
            )
        }
        is HudPropertyEditorType.ColorPicker -> textInput(
            property = property,
            x = x,
            y = y,
            width = width,
            parser = { value -> nullableValue(property, value) { JsonPrimitive(it) } },
            onCommit = { onCommit(it) },
            onInvalidInput = onInvalidInput,
        )
        HudPropertyEditorType.LayoutEditor -> listOf(
            Button.builder(Component.translatable("dynamicrider.hud.editor.property.edit_layout")) { onOpenLayout() }
                .bounds(x, y, width, WIDGET_HEIGHT)
                .build()
        )
        is HudPropertyEditorType.Unsupported -> listOf(unsupportedButton(x, y, width, "dynamicrider.hud.editor.property.unsupported"))
    }

    private fun numericInput(
        property: HudEditableProperty,
        numberType: HudNumberType,
        x: Int,
        y: Int,
        width: Int,
        onCommit: (JsonElement) -> Boolean,
        onInvalidInput: (String) -> Unit,
        onCancelInput: () -> Unit,
    ): List<AbstractWidget> {
        val parser: (String) -> JsonElement = { value ->
            nullableValue(property, value) {
                when (numberType) {
                    HudNumberType.BYTE -> JsonPrimitive(it.toByte())
                    HudNumberType.SHORT -> JsonPrimitive(it.toShort())
                    HudNumberType.INT -> JsonPrimitive(it.toInt())
                    HudNumberType.LONG -> JsonPrimitive(it.toLong())
                    HudNumberType.FLOAT -> JsonPrimitive(it.toFloat())
                    HudNumberType.DOUBLE -> JsonPrimitive(it.toDouble())
                }
            }
        }
        return listOf(
            HudCommitEditBox(
                font = font,
                x = x,
                y = y,
                width = width,
                height = WIDGET_HEIGHT,
                message = Component.translatable(property.nameKey),
                initialValue = displayValue(property.value),
                onCommit = { value ->
                    runCatching { parser(value) }.fold(
                        onSuccess = {
                            onCommit(it)
                        },
                        onFailure = {
                            onInvalidInput(it.message ?: "Invalid value")
                            false
                        },
                    )
                },
                onCancel = onCancelInput,
            ).also { it.setMaxLength(128) }
        )
    }

    private fun textInput(
        property: HudEditableProperty,
        x: Int,
        y: Int,
        width: Int,
        parser: (String) -> JsonElement,
        onCommit: (JsonElement) -> Unit,
        onInvalidInput: (String) -> Unit,
    ): List<AbstractWidget> {
        val applyWidth = 22
        val input = EditBox(
            font,
            x,
            y,
            (width - applyWidth - 2).coerceAtLeast(20),
            WIDGET_HEIGHT,
            Component.translatable(property.nameKey),
        )
        input.setMaxLength(128)
        input.value = displayValue(property.value)
        val apply = Button.builder(Component.literal("✓")) {
            runCatching { parser(input.value) }
                .onSuccess(onCommit)
                .onFailure { onInvalidInput(it.message ?: "Invalid value") }
        }.bounds(x + width - applyWidth, y, applyWidth, WIDGET_HEIGHT).build()
        return listOf(input, apply)
    }

    private fun selector(
        property: HudEditableProperty,
        entries: List<String>,
        x: Int,
        y: Int,
        width: Int,
        onCommit: (JsonElement) -> Unit,
    ): List<AbstractWidget> {
        val current = property.value.jsonPrimitive.content
        val values = (if (property.nullable) listOf(NULL_LITERAL) + entries else entries).distinct()
            .ifEmpty { listOf(current) }
        val initial = current.takeIf(values::contains) ?: values.first()
        return listOf(
            CycleButton.builder<String>(Component::literal)
                .withValues(values)
                .withInitialValue(initial)
                .displayOnlyValue()
                .create(x, y, width, WIDGET_HEIGHT, Component.translatable(property.nameKey)) { _, value ->
                    onCommit(if (property.nullable && value == NULL_LITERAL) JsonNull else JsonPrimitive(value))
                }
        )
    }

    private fun unsupportedButton(x: Int, y: Int, width: Int, key: String): Button =
        Button.builder(Component.translatable(key)) {}
            .bounds(x, y, width, WIDGET_HEIGHT)
            .build()
            .also { it.active = false }

    private fun nullableValue(
        property: HudEditableProperty,
        value: String,
        nonNull: (String) -> JsonElement,
    ): JsonElement = if (property.nullable && value == NULL_LITERAL) JsonNull else nonNull(value)

    private fun displayValue(value: JsonElement): String =
        if (value === JsonNull) NULL_LITERAL else value.jsonPrimitive.content

    private companion object {
        const val WIDGET_HEIGHT = 20
        const val NULL_LITERAL = "null"
    }
}
