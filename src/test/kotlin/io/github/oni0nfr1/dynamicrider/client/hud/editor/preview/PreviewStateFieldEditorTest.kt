package io.github.oni0nfr1.dynamicrider.client.hud.editor.preview

import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PreviewStateFieldEditorTest {
    @Test
    fun `fields follow kart state capabilities`() {
        val v1 = PreviewHudSceneContextFactory.create(KartStateTypes.V1)
        val boat = PreviewHudSceneContextFactory.create(KartStateTypes.BOAT)

        val v1Fields = PreviewStateFieldEditor.fields(v1).map { it.id }
        val boatFields = PreviewStateFieldEditor.fields(boat).map { it.id }

        assertTrue("speed" in v1Fields)
        assertTrue("nitro_gauge" in v1Fields)
        assertTrue("draft_active" in v1Fields)
        assertTrue("exceed_gauge" in v1Fields)
        assertFalse("charger_gauge" in v1Fields)
        assertFalse("speed" in boatFields)
        assertTrue("racing" in boatFields)
    }

    @Test
    fun `updates validate field support type and range`() {
        val context = PreviewHudSceneContextFactory.create(KartStateTypes.JIU)
        val state = assertInstanceOf(PreviewJiuKartState::class.java, context.kartState)

        assertEquals(
            PreviewStateUpdateResult.Updated,
            PreviewStateFieldEditor.update(context, "speed", JsonPrimitive(250.0)),
        )
        assertEquals(250.0, state.speed)
        assertInstanceOf(
            PreviewStateUpdateResult.InvalidValue::class.java,
            PreviewStateFieldEditor.update(context, "nitro_gauge", JsonPrimitive(2.0)),
        )
        assertInstanceOf(
            PreviewStateUpdateResult.FieldNotFound::class.java,
            PreviewStateFieldEditor.update(context, "exceed_gauge", JsonPrimitive(0.5)),
        )
    }

    @Test
    fun `preset application resets previous manual values`() {
        val context = PreviewHudSceneContextFactory.create(KartStateTypes.JIU)
        val state = assertInstanceOf(PreviewJiuKartState::class.java, context.kartState)
        state.isBoosting = true
        state.teamBoostGaugeAvailable = true
        state.teamBoostGauge = 1f

        PreviewStatePresetApplier.apply(context, PreviewStatePresets.RACING)

        assertEquals(169.9, state.speed)
        assertFalse(state.isBoosting)
        assertFalse(state.teamBoostGaugeAvailable)
        assertEquals(0f, state.teamBoostGauge)
        assertThrows(IllegalArgumentException::class.java) {
            PreviewStatePresetApplier.apply(context, PreviewStatePresets.V1_EXCEED)
        }
    }
}
