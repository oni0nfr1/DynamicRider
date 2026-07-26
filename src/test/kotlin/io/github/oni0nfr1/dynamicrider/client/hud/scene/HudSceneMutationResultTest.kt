package io.github.oni0nfr1.dynamicrider.client.hud.scene

import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneContextFactory
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.V1Tachometer
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class HudSceneMutationResultTest {
    private val context = PreviewHudSceneContextFactory.create(KartStateTypes.JIU)

    @Test
    fun `valid compatible spec is added`() {
        val scene = HudScene(context)

        assertSame(HudSceneMutationResult.Applied, scene.addElement("gauge", GradientGaugeBar.Spec()))
    }

    @Test
    fun `invalid compatible spec returns validation errors`() {
        val scene = HudScene(context)

        val result = assertInstanceOf(
            HudSceneMutationResult.InvalidSpec::class.java,
            scene.addElement("gauge", GradientGaugeBar.Spec(width = 3_000)),
        )

        assertEquals("width", result.errors.single().path.toString())
    }

    @Test
    fun `incompatible state is distinguished from invalid spec`() {
        val scene = HudScene(context)

        assertInstanceOf(
            HudSceneMutationResult.IncompatibleState::class.java,
            scene.addElement("tachometer", V1Tachometer.Spec()),
        )
    }
}
