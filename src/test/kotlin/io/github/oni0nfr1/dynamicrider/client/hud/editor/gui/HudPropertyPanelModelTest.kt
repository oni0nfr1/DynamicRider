package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditableProperty
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditablePropertySchema
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditableVariant
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HudPropertyPanelModelTest {
    @Test
    fun `sealed children appear directly after an expanded header`() {
        val child = leaf("style.color")
        val style = sealed("style", listOf(child))
        val trailing = leaf("enabled")

        val rows = HudPropertyPanelModel.rows(
            properties = listOf(style, trailing),
            expandedPaths = setOf(style.path),
        )

        assertEquals(
            listOf("sealed:style:0", "leaf:style.color:1", "leaf:enabled:0"),
            rows.map(::describe),
        )
    }

    @Test
    fun `collapsed parent hides nested sealed headers even when their path is expanded`() {
        val nested = sealed("style.effect", listOf(leaf("style.effect.color")))
        val style = sealed("style", listOf(nested))

        val rows = HudPropertyPanelModel.rows(
            properties = listOf(style),
            expandedPaths = setOf(nested.path),
        )

        assertEquals(listOf("sealed:style:0"), rows.map(::describe))
    }

    @Test
    fun `expanded object exposes its metadata children`() {
        val layout = layout()

        val rows = HudPropertyPanelModel.rows(
            properties = listOf(layout),
            expandedPaths = setOf(layout.path),
        )

        val header = rows.first() as HudPropertyPanelRow.ObjectHeader
        val fields = rows.drop(1).map { it as HudPropertyPanelRow.Leaf }
        assertEquals(layout.path, header.property.path)
        assertEquals(7, fields.size)
        assertEquals(1, fields.single { it.property.path == HudPath.parse("layout.x") }.depth)
    }

    private fun describe(row: HudPropertyPanelRow): String = when (row) {
        is HudPropertyPanelRow.Leaf -> "leaf:${row.property.path}:${row.depth}"
        is HudPropertyPanelRow.SealedHeader -> "sealed:${row.property.path}:${row.depth}"
        is HudPropertyPanelRow.ObjectHeader -> "object:${row.property.path}:${row.depth}"
    }

    private fun leaf(path: String) = HudEditableProperty(
        path = HudPath.parse(path),
        nameKey = "$path.name",
        descriptionKey = null,
        schema = HudEditablePropertySchema.Leaf(HudPropertyEditorType.StringInput),
        value = JsonPrimitive("value"),
        optional = false,
        nullable = false,
    )

    private fun sealed(
        path: String,
        children: List<HudEditableProperty>,
    ) = HudEditableProperty(
        path = HudPath.parse(path),
        nameKey = "$path.name",
        descriptionKey = null,
        schema = HudEditablePropertySchema.Sealed(
            serialName = path,
            discriminator = "type",
            selectedVariant = "default",
            variants = listOf(HudEditableVariant("default", "$path.variant.default")),
            properties = children,
        ),
        value = JsonObject(emptyMap()),
        optional = false,
        nullable = false,
    )

    private fun layout() = HudEditableProperty(
        path = HudPath.parse("layout"),
        nameKey = "layout.name",
        descriptionKey = null,
        schema = HudEditablePropertySchema.Object(
            serialName = "HudLayoutSpec",
            properties = listOf(
                leaf("layout.screenAnchor"),
                leaf("layout.elementAnchor"),
                leaf("layout.scaleX"),
                leaf("layout.scaleY"),
                leaf("layout.x"),
                leaf("layout.y"),
                leaf("layout.zIndex"),
            ),
        ),
        value = JsonObject(
            mapOf(
                "screenAnchor" to JsonPrimitive("TOP_LEFT"),
                "elementAnchor" to JsonPrimitive("TOP_LEFT"),
                "scaleX" to JsonPrimitive(1f),
                "scaleY" to JsonPrimitive(1f),
                "x" to JsonPrimitive(0),
                "y" to JsonPrimitive(0),
                "zIndex" to JsonPrimitive(0f),
            )
        ),
        optional = true,
        nullable = false,
    )
}
