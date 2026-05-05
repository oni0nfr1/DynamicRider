package io.github.oni0nfr1.dynamicrider.client.hud.scene.config

import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBarSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.PlainNitroSlotSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.rankingtable.PlainRankingTableSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedmeter.JiuTachometerSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.timer.HudTimerSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.timer.SpectateHudTimerSpec
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val HudElementSerializersModule = SerializersModule {
    polymorphic(HudElementSpec::class) {
        subclass(GradientGaugeBarSpec::class)
        subclass(PlainNitroSlotSpec::class)
        subclass(PlainRankingTableSpec::class)
        subclass(JiuTachometerSpec::class)
        subclass(HudTimerSpec::class)
        subclass(SpectateHudTimerSpec::class)
    }
}

