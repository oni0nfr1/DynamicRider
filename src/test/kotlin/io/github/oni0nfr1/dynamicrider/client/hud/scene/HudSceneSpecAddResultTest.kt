package io.github.oni0nfr1.dynamicrider.client.hud.scene

import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.DefaultPreviewJiuKartState
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.V1Tachometer
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class HudSceneSpecAddResultTest {
    private val context = PreviewHudSceneContext(KartStateTypes.JIU, DefaultPreviewJiuKartState())

    @Test
    fun `valid compatible spec is added`() {
        val scene = HudScene(context)

        assertSame(HudSceneSpecAddResult.Added, scene.addSpec(GradientGaugeBar.Spec()))
    }

    @Test
    fun `invalid spec is rejected with validation errors`() {
        val scene = HudScene(context)

        val result = assertInstanceOf(
            HudSceneSpecAddResult.InvalidSpec::class.java,
            scene.addSpec(GradientGaugeBar.Spec(width = 3_000)),
        )

        assertEquals("width", result.errors.single().path.toString())
    }

    @Test
    fun `incompatible state is distinguished from invalid spec`() {
        val scene = HudScene(context)

        assertInstanceOf(HudSceneSpecAddResult.IncompatibleState::class.java, scene.addSpec(V1Tachometer.Spec()))
    }
}
