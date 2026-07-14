package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.CompoundElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
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
    data class Spec(
        override val layout: HudLayoutSpec,
        val speedometer: ChargeSpdMeter.Spec,
        val chargerGauge: ChargerGauge.Spec,
        val gauge: ChargeGauge.Spec,
        val icons: ChargeIcons.Spec,
    ) : HudElementSpec<ChargeTachometer, ChargeKartState> {
        override fun requiredStateClass() = ChargeKartState::class.java

        override fun create(
            context: HudSceneContext<ChargeKartState>,
            parent: ElementHolder,
        ) = ChargeTachometer(this, context, parent)
    }
}
