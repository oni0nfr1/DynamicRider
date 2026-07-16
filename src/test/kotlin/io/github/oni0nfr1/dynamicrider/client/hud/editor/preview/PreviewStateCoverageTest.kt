package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PreviewStateCoverageTest {
    @Test
    fun `every registered kart state type has a matching preview context`() {
        KartStateTypes.entries.forEach { stateType ->
            val context = PreviewHudSceneContextFactory.create(stateType)

            assertEquals(stateType, context.kartStateType)
            assertTrue(stateType.stateClass.isInstance(context.kartState))
            assertFalse(PreviewStateFieldEditor.fields(context).isEmpty())
        }
    }

    @Test
    fun `every preset applies exactly to compatible preview states`() {
        KartStateTypes.entries.forEach { stateType ->
            PreviewStatePresets.entries.forEach { preset ->
                val context = PreviewHudSceneContextFactory.create(stateType)
                if (preset.isCompatibleWith(context.kartState)) {
                    assertDoesNotThrow(
                        { PreviewStatePresetApplier.apply(context, preset) },
                        "${preset.id} should apply to ${stateType.id}",
                    )
                } else {
                    assertThrows(
                        IllegalArgumentException::class.java,
                        { PreviewStatePresetApplier.apply(context, preset) },
                        "${preset.id} should reject ${stateType.id}",
                    )
                }
            }
        }
    }

    @Test
    fun `every preset name is translated in supported languages`() {
        val requiredKeys = PreviewStatePresets.entries.mapTo(linkedSetOf(), PreviewStatePreset::displayNameKey)

        listOf("en_us", "ko_kr").forEach { locale ->
            val availableKeys = resource("/assets/dynrider/lang/$locale.json").let {
                Json.parseToJsonElement(it).jsonObject.keys
            }
            val missingKeys = requiredKeys - availableKeys
            assertTrue(missingKeys.isEmpty(), "$locale is missing preview preset translations: $missingKeys")
        }
    }

    private fun resource(path: String): String =
        checkNotNull(javaClass.getResourceAsStream(path)) { "Missing test resource: $path" }
            .bufferedReader()
            .use { it.readText() }
}
