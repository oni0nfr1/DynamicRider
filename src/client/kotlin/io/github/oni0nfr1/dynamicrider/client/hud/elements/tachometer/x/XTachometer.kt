package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.x

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.CompoundElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudElementInfo
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudLayout
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.state.XKartState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class XTachometer(
    spec: Spec,
    context: HudSceneContext<XKartState>,
    parent: ElementHolder,
) : CompoundElement<XKartState>(spec.layout, context, parent) {

    init {
        addChild(spec.statusRing)
        addChild(spec.speedometer)
    }

    override val width: Int
        get() = XStatusRing.RING_ATLAS.cellWidth
    override val height: Int
        get() = XStatusRing.RING_ATLAS.cellHeight

    @Serializable
    @SerialName("X_TACHOMETER")
    @HudElementInfo(category = "tachometer")
    data class Spec(
        @HudLayout
        override val layout: HudLayoutSpec = HudLayoutSpec(),
        val statusRing: XStatusRing.Spec = XStatusRing.Spec(),
        val speedometer: XSpdMeter.Spec = XSpdMeter.Spec(
            layout = HudLayoutSpec(x = 90, y = 37),
        ),
    ) : HudElementSpec<XTachometer, XKartState> {
        override fun requiredStateClass() = XKartState::class.java

        override fun create(
            context: HudSceneContext<XKartState>,
            parent: ElementHolder,
        ) = XTachometer(this, context, parent)
    }
}
