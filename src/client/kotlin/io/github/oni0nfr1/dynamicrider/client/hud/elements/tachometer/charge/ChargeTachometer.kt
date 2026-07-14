package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.CompoundElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudElementInfo
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudLayout
import io.github.oni0nfr1.dynamicrider.client.hud.state.ChargeKartState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ChargeTachometer(
    spec: Spec,
    context: HudSceneContext<ChargeKartState>,
    parent: ElementHolder,
) : CompoundElement<ChargeKartState>(spec.layout, context, parent) {

    init {
        addChild(spec.speedometer)
        addChild(spec.chargerGauge)
        addChild(spec.gauge)
        addChild(spec.icons)
    }

    override val width: Int
        get() = ChargeSpdMeter.META.backgroundWidth
    override val height: Int
        get() = ChargeSpdMeter.META.backgroundHeight

    @Serializable
    @SerialName("CHARGE_TACHOMETER")
    @HudElementInfo(category = "tachometer")
    data class Spec(
        @HudLayout
        override val layout: HudLayoutSpec = HudLayoutSpec(),
        val speedometer: ChargeSpdMeter.Spec = ChargeSpdMeter.Spec(),
        val chargerGauge: ChargerGauge.Spec = ChargerGauge.Spec(
            layout = HudLayoutSpec(x = 0, y = 27),
        ),
        val gauge: ChargeGauge.Spec = ChargeGauge.Spec(
            layout = HudLayoutSpec(x = 79, y = 2),
        ),
        val icons: ChargeIcons.Spec = ChargeIcons.Spec(
            layout = HudLayoutSpec(x = 152, y = 91),
        ),
    ) : HudElementSpec<ChargeTachometer, ChargeKartState> {
        override fun requiredStateClass() = ChargeKartState::class.java

        override fun create(
            context: HudSceneContext<ChargeKartState>,
            parent: ElementHolder,
        ) = ChargeTachometer(this, context, parent)
    }
}
