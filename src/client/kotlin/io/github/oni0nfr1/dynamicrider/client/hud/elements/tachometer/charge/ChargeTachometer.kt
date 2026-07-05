package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.CompoundElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.skid.client.api.engine.ChargeEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ChargeTachometer(
    spec: Spec,
    kart: KartRef.Specific<ChargeEngine>,
    parent: ElementHolder,
) : CompoundElement<ChargeEngine>(spec.layout, kart, parent) {

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

    class Builder : HudElementBuilder<Spec>() {
        val speedometer = ChargeSpdMeter.Builder()
        val chargerGauge = ChargerGauge.Builder()
        val gauge = ChargeGauge.Builder()
        val icons = ChargeIcons.Builder()

        override fun build(layout: HudLayoutSpec) = Spec(
            layout = layout,
            speedometer = speedometer.build(),
            chargerGauge = chargerGauge.build(),
            gauge = gauge.build(),
            icons = icons.build(),
        )
    }

    @Serializable
    @SerialName("CHARGE_TACHOMETER")
    data class Spec(
        override val layout: HudLayoutSpec,
        val speedometer: ChargeSpdMeter.Spec,
        val chargerGauge: ChargerGauge.Spec,
        val gauge: ChargeGauge.Spec,
        val icons: ChargeIcons.Spec,
    ) : HudElementSpec<ChargeTachometer, ChargeEngine> {
        override fun requiredEngineClass() = ChargeEngine::class.java

        override fun create(
            kart: KartRef.Specific<ChargeEngine>,
            parent: ElementHolder,
        ) = ChargeTachometer(this, kart, parent)
    }
}
