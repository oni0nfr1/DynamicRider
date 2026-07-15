package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
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

    @Test
    fun `resolve spec returns a valid custom scene without creating a runtime scene`() {
        val repository = HudSceneRepository(root)
        val spec = HudSceneSpec(
            elementIds = listOf("gauge"),
            elements = listOf(GradientGaugeBar.Spec(width = 180)),
        )
        val path = repository.saveCustom(HudSceneMode.RIDE, KartStateTypes.JIU, spec).getOrThrow()

        val result = assertInstanceOf(
            HudSceneSpecResolution.Resolved::class.java,
            repository.resolveSpec(HudSceneMode.RIDE, KartStateTypes.JIU),
        )

        assertEquals(spec, result.spec)
        assertEquals(path, result.sourcePath)
        assertEquals(HudSceneSource.CUSTOM_CONFIG, result.source)
        assertTrue(result.diagnostics.isEmpty())
    }

    @Test
    fun `invalid custom scene is preserved and reported when no resource fallback exists`() {
        val repository = HudSceneRepository(root)
        val path = repository.saveCustom(
            HudSceneMode.RIDE,
            KartStateTypes.JIU,
            HudSceneSpec(
                elementIds = listOf("gauge"),
                elements = listOf(GradientGaugeBar.Spec(width = 3_000)),
            ),
        ).getOrThrow()

        val result = assertInstanceOf(
            HudSceneSpecResolution.Failed::class.java,
            repository.resolveSpec(HudSceneMode.RIDE, KartStateTypes.JIU),
        )

        assertTrue(result.errors.any { it is HudSceneLoadError.InvalidElement })
        assertTrue(result.errors.any { it is HudSceneLoadError.FileNotFound })
        assertTrue(Files.exists(path))
    }
}
