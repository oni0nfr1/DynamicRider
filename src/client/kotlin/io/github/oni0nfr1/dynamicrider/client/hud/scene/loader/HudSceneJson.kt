package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.PlainNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.DynNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.rankingtable.PlainRankingTable
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedometer.JiuStyleSpdMeter
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge.ChargeGauge
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge.ChargeIcons
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge.ChargeSpdMeter
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge.ChargeTachometer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge.ChargerGauge
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu.JiuGauge
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu.JiuIcons
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu.JiuSpdMeter
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu.JiuTachometer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.V1Tachometer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.timer.HudTimer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.timer.SpectateHudTimer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val HudElementSerializersModule = SerializersModule {
    polymorphic(HudElementSpec::class) {
        subclass(GradientGaugeBar.Spec::class)
        subclass(PlainNitroSlot.Spec::class)
        subclass(DynNitroSlot.Spec::class)
        subclass(PlainRankingTable.Spec::class)
        subclass(JiuStyleSpdMeter.Spec::class)
        subclass(HudTimer.Spec::class)
        subclass(SpectateHudTimer.Spec::class)
        subclass(ChargeTachometer.Spec::class)
        subclass(ChargeSpdMeter.Spec::class)
        subclass(ChargeGauge.Spec::class)
        subclass(ChargerGauge.Spec::class)
        subclass(ChargeIcons.Spec::class)

        // hud.elements.tachometer.jiu
        subclass(JiuTachometer.Spec::class)
        subclass(JiuSpdMeter.Spec::class)
        subclass(JiuIcons.Spec::class)
        subclass(JiuGauge.Spec::class)
        subclass(V1Tachometer.Spec::class)
    }
}
