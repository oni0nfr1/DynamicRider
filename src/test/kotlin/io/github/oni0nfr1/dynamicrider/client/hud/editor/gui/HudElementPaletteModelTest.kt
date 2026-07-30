package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudElementPaletteEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HudElementPaletteModelTest {
    @Test
    fun `groups categories by first appearance while preserving element order`() {
        val rows = HudElementPaletteModel.rows(
            listOf(
                entry("gauge-a", "gauge"),
                entry("nitro-a", "nitro"),
                entry("gauge-b", "gauge"),
                entry("nitro-b", "nitro"),
            )
        )

        assertEquals(
            listOf(
                "header:gauge",
                "element:gauge-a",
                "element:gauge-b",
                "header:nitro",
                "element:nitro-a",
                "element:nitro-b",
            ),
            rows.map {
                when (it) {
                    is HudElementPaletteRow.CategoryHeader -> "header:${it.category}"
                    is HudElementPaletteRow.Element -> "element:${it.entry.typeId}"
                }
            },
        )
    }

    @Test
    fun `does not create headers for an empty palette`() {
        assertTrue(HudElementPaletteModel.rows(emptyList()).isEmpty())
    }

    @Test
    fun `collapsed category keeps its header and hides only its elements`() {
        val rows = HudElementPaletteModel.rows(
            entries = listOf(
                entry("gauge-a", "gauge"),
                entry("nitro-a", "nitro"),
            ),
            collapsedCategories = setOf("gauge"),
        )

        assertEquals(
            listOf("header:gauge:true", "header:nitro:false", "element:nitro-a"),
            rows.map {
                when (it) {
                    is HudElementPaletteRow.CategoryHeader ->
                        "header:${it.category}:${it.collapsed}"
                    is HudElementPaletteRow.Element -> "element:${it.entry.typeId}"
                }
            },
        )
    }

    private fun entry(typeId: String, category: String) = HudElementPaletteEntry(
        typeId = typeId,
        nameKey = "element.$typeId",
        category = category,
        categoryNameKey = "category.$category",
        icon = null,
    )
}
