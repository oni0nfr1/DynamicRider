package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditableProperty
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditablePropertySchema
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath

sealed interface HudPropertyPanelRow {
    val property: HudEditableProperty
    val depth: Int

    data class Leaf(
        override val property: HudEditableProperty,
        override val depth: Int,
    ) : HudPropertyPanelRow

    sealed interface GroupHeader : HudPropertyPanelRow {
        val expanded: Boolean
    }

    data class SealedHeader(
        override val property: HudEditableProperty,
        override val depth: Int,
        val schema: HudEditablePropertySchema.Sealed,
        override val expanded: Boolean,
    ) : GroupHeader

    data class ObjectHeader(
        override val property: HudEditableProperty,
        override val depth: Int,
        val schema: HudEditablePropertySchema.Object,
        override val expanded: Boolean,
    ) : GroupHeader
}

/** 펼친 container의 자식만 포함하는 스크롤 가능한 속성 행 snapshot을 만든다. */
object HudPropertyPanelModel {
    fun rows(
        properties: List<HudEditableProperty>,
        expandedPaths: Set<HudPath>,
    ): List<HudPropertyPanelRow> = buildList {
        fun addProperties(properties: List<HudEditableProperty>, depth: Int) {
            properties.forEach { property ->
                val sealed = property.schema as? HudEditablePropertySchema.Sealed
                val objectSchema = property.schema as? HudEditablePropertySchema.Object
                val expanded = property.path in expandedPaths
                when {
                    objectSchema != null -> {
                        add(HudPropertyPanelRow.ObjectHeader(property, depth, objectSchema, expanded))
                        if (expanded) addProperties(objectSchema.properties, depth + 1)
                    }
                    sealed != null -> {
                        add(HudPropertyPanelRow.SealedHeader(property, depth, sealed, expanded))
                        if (expanded) addProperties(sealed.properties, depth + 1)
                    }
                    else -> add(HudPropertyPanelRow.Leaf(property, depth))
                }
            }
        }
        addProperties(properties, 0)
    }
}
