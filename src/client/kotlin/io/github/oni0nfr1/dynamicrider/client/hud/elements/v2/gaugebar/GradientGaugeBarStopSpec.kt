package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.gaugebar

import kotlinx.serialization.Serializable

@Serializable
data class GradientGaugeBarStopSpec(
    val offset: Int,
    val color: Int,
)
