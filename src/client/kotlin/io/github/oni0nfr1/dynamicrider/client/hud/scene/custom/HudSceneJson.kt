package io.github.oni0nfr1.dynamicrider.client.hud.scene.custom

import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.PlainNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.rankingtable.PlainRankingTable
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedmeter.JiuStyleSpdMeter
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.ChargeTachometer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.timer.HudTimer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.timer.SpectateHudTimer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val HudElementSerializersModule = SerializersModule {
    polymorphic(HudElementSpec::class) {
        subclass(GradientGaugeBar.Spec::class)
        subclass(PlainNitroSlot.Spec::class)
        subclass(PlainRankingTable.Spec::class)
        subclass(JiuStyleSpdMeter.Spec::class)
        subclass(HudTimer.Spec::class)
        subclass(SpectateHudTimer.Spec::class)
        subclass(ChargeTachometer.Spec::class)
    }
}

