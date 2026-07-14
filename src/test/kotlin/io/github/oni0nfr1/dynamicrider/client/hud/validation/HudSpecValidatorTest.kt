package io.github.oni0nfr1.dynamicrider.client.hud.validation

import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu.JiuSpdMeter
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu.JiuTachometer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class HudSpecValidatorTest {
    @Test
    fun `default registered spec is valid`() {
        assertSame(HudSpecValidationResult.Valid, HudSpecValidator.validate(GradientGaugeBar.Spec()))
    }

    @Test
    fun `all range violations are returned with property paths`() {
        val spec = GradientGaugeBar.Spec(thickness = 0, width = 3_000, padding = -1)

        val result = assertInstanceOf(
            HudSpecValidationResult.Invalid::class.java,
            HudSpecValidator.validate(spec),
        )

        assertEquals(setOf("thickness", "width", "padding"), result.errors.map { it.path.toString() }.toSet())
        assertEquals(setOf(HudSpecValidationErrorCode.OUT_OF_RANGE), result.errors.map { it.code }.toSet())
    }

    @Test
    fun `nested compound spec is validated recursively`() {
        val spec = JiuTachometer.Spec(
            speedometer = JiuSpdMeter.Spec(
                layout = HudLayoutSpec(scaleX = Float.NaN),
            ),
        )

        val result = assertInstanceOf(
            HudSpecValidationResult.Invalid::class.java,
            HudSpecValidator.validate(spec),
        )

        assertEquals(listOf("speedometer.layout.scaleX"), result.errors.map { it.path.toString() })
        assertEquals(HudSpecValidationErrorCode.NON_FINITE_NUMBER, result.errors.single().code)
    }
}
