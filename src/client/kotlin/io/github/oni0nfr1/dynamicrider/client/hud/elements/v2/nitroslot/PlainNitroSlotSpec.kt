package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.nitroslot

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec
import kotlinx.serialization.Serializable

@Serializable
data class PlainNitroSlotSpec(
    override val layout: HudLayoutSpec,
    val slotIndex: Int = 1,
    val hideUntilOccupied: Boolean = false,
    val keepVisibleAfterOccupied: Boolean = true,
    val iconSize: Int = 32,
    val boxPadding: Int = 5,
    val boxColor: Int = 0x80000000.toInt(),
) : HudElementSpec<PlainNitroSlot>() {
    override fun create(): PlainNitroSlot = PlainNitroSlot(this)
}
