package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.CompoundElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudElementInfo
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudLayout
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
        addChild(spec::speedometer)
        addChild(spec::icons)
        addChild(spec::gauge)
    }

    override val width: Int
        get() = JiuSpdMeter.BG_ATLAS.cellWidth
    override val height: Int
        get() = JiuSpdMeter.BG_ATLAS.cellHeight

    @Serializable
    @SerialName("JIU_TACHOMETER")
    @HudElementInfo(category = "tachometer")
    data class Spec(
        @HudLayout
        override val layout: HudLayoutSpec = HudLayoutSpec(),
        val speedometer: JiuSpdMeter.Spec = JiuSpdMeter.Spec(),
        val icons: JiuIcons.Spec = JiuIcons.Spec(
            layout = HudLayoutSpec(
                screenAnchor = HudAnchor.TOP_CENTER,
                elementAnchor = HudAnchor.TOP_CENTER,
                y = 8,
            ),
        ),
        val gauge: JiuGauge.Spec = JiuGauge.Spec(
            layout = HudLayoutSpec(
                screenAnchor = HudAnchor.TOP_CENTER,
                elementAnchor = HudAnchor.BOTTOM_CENTER,
                y = -6,
            ),
        ),
    ) : HudElementSpec<JiuTachometer, JiuKartState> {
        override fun requiredStateClass() = JiuKartState::class.java

        override fun create(
            context: HudSceneContext<JiuKartState>,
            parent: ElementHolder
        ) = JiuTachometer(this, context, parent)

    }
}
