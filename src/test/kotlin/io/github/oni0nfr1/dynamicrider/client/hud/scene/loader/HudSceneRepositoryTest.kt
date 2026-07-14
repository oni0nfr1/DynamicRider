package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class HudSceneRepositoryTest {
    @TempDir
    lateinit var root: Path

    @Test
    fun `save writes decodable custom scene and delete returns to missing state`() {
        val repository = HudSceneRepository(root)
        val original = HudSceneSpec(
            elementIds = listOf("gauge"),
            elements = listOf(GradientGaugeBar.Spec(width = 180)),
        )

        val savedPath = repository.saveCustom(HudSceneMode.RIDE, KartStateTypes.JIU, original).getOrThrow()

        assertTrue(Files.isRegularFile(savedPath))
        assertEquals(original, HudSceneCodec.decode(Files.readString(savedPath)))
        assertTrue(repository.deleteCustom(HudSceneMode.RIDE, KartStateTypes.JIU).getOrThrow())
        assertFalse(Files.exists(savedPath))
        assertFalse(repository.deleteCustom(HudSceneMode.RIDE, KartStateTypes.JIU).getOrThrow())
    }
}
