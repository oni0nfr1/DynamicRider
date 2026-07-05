package io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.bridge

import io.github.oni0nfr1.dynamicrider.client.rider.backend.inventory.KartTeamBoostCounter
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

class SkidNitroSlot(val kart: KartRef.Specific<NitroEngine>) : NitroSlot {
    override val maxBoost: Int
        get() = kart.accessEngine { it.maxBoost } ?: 2
    override val nitro: Int
        get() = kart.accessEngine { it.tachometer?.nitro } ?: 0
    override val teamNitro: Int
        get() = KartTeamBoostCounter.boostCount
}