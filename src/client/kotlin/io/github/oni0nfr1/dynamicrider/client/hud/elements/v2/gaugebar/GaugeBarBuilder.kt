package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.gaugebar

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec

class GaugeBarBuilder : HudElementBuilder<GaugeBarSpec>() {
    var thickness: Int = 8
    var width: Int = 120
    var padding: Int = 2
    var boxColor: Int = 0x80000000.toInt()
    var gaugeAlpha: Int = 0xFF
    var targetGaugeAlpha: Int = 0x80
    var smoothing: Double = 1.0
    private val gradientStops: MutableList<GaugeBarStopSpec> =
        defaultGradientGaugeBarStops().toMutableList()

    fun gradientStop(offset: Int, color: Int) {
        gradientStops += GaugeBarStopSpec(offset = offset, color = color)
    }

    fun gradientStops(vararg stops: GaugeBarStopSpec) {
        gradientStops.clear()
        gradientStops.addAll(stops)
    }

    override fun build(layout: HudLayoutSpec): GaugeBarSpec {
        return GaugeBarSpec(
            layout = layout,
            thickness = thickness,
            width = width,
            padding = padding,
            boxColor = boxColor,
            gaugeAlpha = gaugeAlpha,
            targetGaugeAlpha = targetGaugeAlpha,
            smoothing = smoothing,
            gradientStops = gradientStops.toList(),
        )
    }
}
