package io.github.oni0nfr1.dynamicrider.client.hud.elements.registry

import io.github.oni0nfr1.dynamicrider.client.hud.elements.debug.EditorPropertyStressElement
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HudElementTypeRegistryTest {
    @Test
    fun `editor stress element exposes enough properties for manual scrolling`() {
        val type = HudElementTypeRegistry.EDITOR_PROPERTY_STRESS_TEST

        assertEquals(EditorPropertyStressElement.Spec::class, type.specClass)
        assertTrue(type.metadata.properties.size >= 28)
        assertTrue(type.metadata.properties.any { it.editor is HudPropertyEditorType.BooleanToggle })
        assertTrue(type.metadata.properties.any { it.editor is HudPropertyEditorType.StringInput })
        assertTrue(type.metadata.properties.any { it.editor is HudPropertyEditorType.EnumSelector })
        assertTrue(type.metadata.properties.any { it.editor is HudPropertyEditorType.Slider })
        assertTrue(type.metadata.properties.any { it.editor is HudPropertyEditorType.ColorPicker })
    }

    @Test
    fun `every entry has a unique id and spec class`() {
        val entries = HudElementTypeRegistry.entries

        assertEquals(entries.size, entries.map { it.id }.distinct().size)
        assertEquals(entries.size, entries.map { it.specClass }.distinct().size)
    }

    @Test
    fun `every entry can create and resolve its default spec`() {
        HudElementTypeRegistry.entries.forEach { type ->
            val spec = type.createDefaultSpec()

            assertSame(type, HudElementTypeRegistry.byId(type.id))
            assertSame(type, HudElementTypeRegistry.bySpec(spec))
            assertTrue(type.specClass.isInstance(spec))
        }
    }

    @Test
    fun `every entry exposes usable editor metadata`() {
        HudElementTypeRegistry.entries.forEach { type ->
            val metadata = type.metadata

            assertEquals(type.id, metadata.serialName)
            assertTrue(metadata.nameKey.isNotBlank())
            assertTrue(metadata.categoryNameKey.isNotBlank())
            assertFalse(metadata.properties.isEmpty())
            assertNotNull(metadata.properties.singleOrNull { it.editor is HudPropertyEditorType.LayoutEditor })
        }
    }

    @Test
    fun `editor metadata keys exist in every supported language`() {
        val requiredKeys = HudElementTypeRegistry.entries.flatMap { type ->
            buildList {
                add(type.metadata.nameKey)
                add(type.metadata.categoryNameKey)
                type.metadata.properties.forEach { property ->
                    add(property.nameKey)
                    property.descriptionKey?.let(::add)
                }
            }
        }.toSet()

        listOf("en_us", "ko_kr").forEach { locale ->
            val availableKeys = resource("/assets/dynrider/lang/$locale.json").let {
                Json.parseToJsonElement(it).jsonObject.keys
            }
            val missingKeys = requiredKeys - availableKeys
            assertTrue(missingKeys.isEmpty(), "$locale is missing metadata translations: $missingKeys")
        }
    }

    private fun resource(path: String): String =
        checkNotNull(javaClass.getResourceAsStream(path)) { "Missing test resource: $path" }
            .bufferedReader()
            .use { it.readText() }
}
