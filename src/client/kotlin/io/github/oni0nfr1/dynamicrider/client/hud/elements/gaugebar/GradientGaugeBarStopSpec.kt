package io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar

import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HexColorSerdes
import kotlinx.serialization.Serializable

@Serializable
data class GradientGaugeBarStopSpec(
    val offset: Int,
    @Serializable(with = HexColorSerdes::class)
    val color: Int,
)
