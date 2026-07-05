package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.CompoundElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.skid.client.api.engine.JiuEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class JiuTachometer(
    spec: Spec,
    kart: KartRef.Specific<JiuEngine>,
    parent: ElementHolder,
) : CompoundElement<JiuEngine>(spec.layout, kart, parent)
{
    init {
        addChild(spec.speedometer)
        addChild(spec.icons)
        addChild(spec.gauge)
    }

    override val width: Int
        get() = JiuSpdMeter.BG_ATLAS.cellWidth
    override val height: Int
        get() = JiuSpdMeter.BG_ATLAS.cellHeight

    class Builder : HudElementBuilder<Spec>() {
        val speedometer = JiuSpdMeter.Builder()
        val icons = JiuIcons.Builder()
        val gauge = JiuGauge.Builder()

        override fun build(layout: HudLayoutSpec) = Spec(
            layout,
            speedometer.build(),
            icons.build(),
            gauge.build(),
        )
    }

    @Serializable
    @SerialName("JIU_TACHOMETER")
    data class Spec(
        override val layout: HudLayoutSpec,
        val speedometer: JiuSpdMeter.Spec,
        val icons: JiuIcons.Spec,
        val gauge: JiuGauge.Spec,
    ) : HudElementSpec<JiuTachometer, JiuEngine> {
        override fun requiredEngineClass() = JiuEngine::class.java

        override fun create(
            kart: KartRef.Specific<JiuEngine>,
            parent: ElementHolder
        ) = JiuTachometer(this, kart, parent)

    }
}
