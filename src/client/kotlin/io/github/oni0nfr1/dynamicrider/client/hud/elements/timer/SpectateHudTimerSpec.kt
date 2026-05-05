package io.github.oni0nfr1.dynamicrider.client.hud.elements.timer

import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HexColorSerdes
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("spectate_hud_timer")
data class SpectateHudTimerSpec(
    override val layout: HudLayoutSpec,
    val minWidth: Int = 100,
    @Serializable(with = HexColorSerdes::class)
    val txtColor: Int = 0xFFFFFFFF.toInt(),
) : HudElementSpec<SpectateHudTimer, KartEngine>() {
    override fun requiredEngineClass(): Class<out KartEngine> = KartEngine::class.java

    override fun create(kart: KartRef.Specific<KartEngine>): SpectateHudTimer = SpectateHudTimer(this, kart)
}
