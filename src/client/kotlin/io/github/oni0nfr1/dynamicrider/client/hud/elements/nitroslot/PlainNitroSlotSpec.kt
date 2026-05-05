package io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot

import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HexColorSerdes
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("plain_nitro_slot")
data class PlainNitroSlotSpec(
    override val layout: HudLayoutSpec,
    val slotIndex: Int = 1,
    val hideUntilOccupied: Boolean = false,
    val keepVisibleAfterOccupied: Boolean = true,
    val iconSize: Int = 32,
    val boxPadding: Int = 5,
    @Serializable(with = HexColorSerdes::class)
    val boxColor: Int = 0x80000000.toInt(),
) : HudElementSpec<PlainNitroSlot, NitroEngine>() {
    override fun requiredEngineClass(): Class<out NitroEngine> = NitroEngine::class.java

    override fun create(kart: KartRef.Specific<NitroEngine>): PlainNitroSlot = PlainNitroSlot(this, kart)
}
