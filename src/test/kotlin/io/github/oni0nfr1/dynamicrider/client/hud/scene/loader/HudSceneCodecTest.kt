package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.PlainNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import kotlinx.serialization.SerializationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class HudSceneCodecTest {
    @Test
    fun `scene round trip preserves element ids and concrete specs`() {
        val original = HudSceneSpec(
            elementIds = listOf("gauge", "slot"),
            elements = listOf(GradientGaugeBar.Spec(width = 200), PlainNitroSlot.Spec(slotIndex = 2)),
        )

        val decoded = HudSceneCodec.decode(HudSceneCodec.encode(original))

        assertEquals(original, decoded)
    }

    @Test
    fun `unsupported format version is rejected`() {
        val content = """{"formatVersion":999,"elements":[]}"""

        assertThrows(SerializationException::class.java) { HudSceneCodec.decode(content) }
    }

    @Test
    fun `duplicate or mismatched element ids are rejected`() {
        assertThrows(SerializationException::class.java) {
            HudSceneCodec.decode(
                """{"formatVersion":1,"elementIds":["same","same"],"elements":[{"type":"RIDE_TIMER"},{"type":"RIDE_TIMER"}]}"""
            )
        }
        assertThrows(SerializationException::class.java) {
            HudSceneCodec.decode(
                """{"formatVersion":1,"elementIds":["only-one"],"elements":[{"type":"RIDE_TIMER"},{"type":"RIDE_TIMER"}]}"""
            )
        }
    }

    @Test
    fun `all built-in scenes decode and contain compatible element specs`() {
        val modes = listOf("ride", "spectate")
        val stateIds = listOf("default", "charge", "jiu", "v1")

        modes.forEach { mode ->
            stateIds.forEach { stateId ->
                val path = "/assets/dynrider/hud/$mode/$stateId.json"
                val scene = HudSceneCodec.decode(resource(path))
                val stateType = KartStateTypes.byId(stateId)
                if (stateType != null) {
                    scene.elements.forEach { spec ->
                        val elementType = checkNotNull(HudElementTypeRegistry.bySpec(spec))
                        org.junit.jupiter.api.Assertions.assertTrue(
                            elementType.accepts(stateType),
                            "${elementType.id} is incompatible with $path",
                        )
                    }
                }
            }
        }
    }

    private fun resource(path: String): String =
        checkNotNull(javaClass.getResourceAsStream(path)) { "Missing test resource: $path" }
            .bufferedReader()
            .use { it.readText() }
}
