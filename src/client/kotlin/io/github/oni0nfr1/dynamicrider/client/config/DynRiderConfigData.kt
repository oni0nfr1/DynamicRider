package io.github.oni0nfr1.dynamicrider.client.config

import kotlinx.serialization.Serializable

@Serializable
data class DynRiderConfigData(
    val isModEnabled: Boolean = true,
    val hudFont: String = "VANILLA"
)
