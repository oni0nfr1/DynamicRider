package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudElementPaletteEntry

sealed interface HudElementPaletteRow {
    data class CategoryHeader(
        val category: String,
        val nameKey: String,
        val collapsed: Boolean,
    ) : HudElementPaletteRow

    data class Element(
        val entry: HudElementPaletteEntry,
    ) : HudElementPaletteRow
}

/** 요소 순서를 보존하면서 같은 category의 팔레트 항목을 하나의 section으로 묶는다. */
object HudElementPaletteModel {
    fun rows(
        entries: List<HudElementPaletteEntry>,
        collapsedCategories: Set<String> = emptySet(),
    ): List<HudElementPaletteRow> =
        entries.groupBy(HudElementPaletteEntry::category).flatMap { (category, elements) ->
            buildList {
                val collapsed = category in collapsedCategories
                add(
                    HudElementPaletteRow.CategoryHeader(
                        category = category,
                        nameKey = elements.first().categoryNameKey,
                        collapsed = collapsed,
                    )
                )
                if (!collapsed) {
                    elements.forEach { add(HudElementPaletteRow.Element(it)) }
                }
            }
        }
}
