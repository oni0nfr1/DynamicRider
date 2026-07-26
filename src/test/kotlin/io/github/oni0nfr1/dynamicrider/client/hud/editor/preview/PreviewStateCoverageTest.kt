package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import io.github.oni0nfr1.dynamicrider.client.hud.state.DriftKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.DualBoostKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.DraftKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.ExceedKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.GearLikeKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.InstantBoostKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.MKLikeKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.SpeedKartState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PreviewStateCoverageTest {
    private val capabilities = linkedMapOf(
        "drift" to DriftKartState::class.java,
        "speed" to SpeedKartState::class.java,
        "nitro" to NitroKartState::class.java,
        "gearlike" to GearLikeKartState::class.java,
        "instant" to InstantBoostKartState::class.java,
        "dual" to DualBoostKartState::class.java,
        "draft" to DraftKartState::class.java,
        "exceed" to ExceedKartState::class.java,
        "mk_like" to MKLikeKartState::class.java,
    )

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
    fun `kart state types preserve the skid engine capability matrix`() {
        val nitro = setOf("drift", "speed", "nitro")
        val instantDraft = nitro + setOf("instant", "draft")
        val expected = mapOf(
            "x" to instantDraft + "dual",
            "ex" to instantDraft + "dual",
            "jiu" to instantDraft,
            "new" to instantDraft,
            "z7" to instantDraft,
            "v1" to instantDraft + setOf("dual", "exceed"),
            "a2" to instantDraft,
            "legacy" to instantDraft + "dual",
            "pro" to instantDraft,
            "rushplus" to instantDraft + "exceed",
            "charge" to instantDraft,
            "sr" to instantDraft,
            "n1" to instantDraft,
            "rx" to instantDraft,
            "key" to nitro,
            "gear" to setOf("drift", "speed", "gearlike", "draft"),
            "f1" to setOf("drift", "speed", "gearlike", "draft"),
            "rally" to setOf("drift", "speed", "gearlike", "draft"),
            "mk" to setOf("drift", "draft", "mk_like"),
            "ds" to setOf("drift", "draft", "mk_like"),
            "boat" to emptySet(),
        )

        KartStateTypes.entries.forEach { stateType ->
            val actual = capabilities.filterValues(stateType::accepts).keys
            assertEquals(expected.getValue(stateType.id), actual, stateType.id)

            val preview = PreviewHudSceneContextFactory.create(stateType).kartState
            val previewCapabilities = capabilities.filterValues { it.isInstance(preview) }.keys
            assertEquals(expected.getValue(stateType.id), previewCapabilities, "${stateType.id} preview")
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

    @Test
    fun `every preview state field is translated in supported languages`() {
        val requiredKeys = KartStateTypes.entries
            .flatMap { PreviewStateFieldEditor.fields(PreviewHudSceneContextFactory.create(it)) }
            .mapTo(linkedSetOf(), PreviewStateField::nameKey)

        listOf("en_us", "ko_kr").forEach { locale ->
            val availableKeys = resource("/assets/dynrider/lang/$locale.json").let {
                Json.parseToJsonElement(it).jsonObject.keys
            }
            val missingKeys = requiredKeys - availableKeys
            assertTrue(missingKeys.isEmpty(), "$locale is missing preview field translations: $missingKeys")
        }
    }

    private fun resource(path: String): String =
        checkNotNull(javaClass.getResourceAsStream(path)) { "Missing test resource: $path" }
            .bufferedReader()
            .use { it.readText() }
}
