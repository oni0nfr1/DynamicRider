package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.timer

import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec
import kotlinx.serialization.Serializable

@Serializable
data class HudTimerSpec(
    override val layout: HudLayoutSpec,
    val minWidth: Int = 100,
    val txtColor: Int = 0xFFFFFFFF.toInt(),
) : HudElementSpec<HudTimer>() {
    override fun create(): HudTimer = HudTimer(this)
}
