package io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.bridge

import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState

class SkidNitroSlot(private val state: NitroKartState) : NitroSlot {
    override val maxBoost: Int
        get() = state.maxBoost
    override val nitro: Int
        get() = state.nitro
    override val teamNitro: Int
        get() = state.teamNitro
}
