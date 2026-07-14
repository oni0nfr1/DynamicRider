package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.CompoundElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.JiuKartState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class JiuTachometer(
    spec: Spec,
    context: HudSceneContext<JiuKartState>,
    parent: ElementHolder,
) : CompoundElement<JiuKartState>(spec.layout, context, parent)
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

    @Serializable
    @SerialName("JIU_TACHOMETER")
    data class Spec(
        override val layout: HudLayoutSpec,
        val speedometer: JiuSpdMeter.Spec,
        val icons: JiuIcons.Spec,
        val gauge: JiuGauge.Spec,
    ) : HudElementSpec<JiuTachometer, JiuKartState> {
        override fun requiredStateClass() = JiuKartState::class.java

        override fun create(
            context: HudSceneContext<JiuKartState>,
            parent: ElementHolder
        ) = JiuTachometer(this, context, parent)

    }
}
