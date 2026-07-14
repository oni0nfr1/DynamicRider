package io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/** HUD 속성 패널에 표시할 선택적 번역 key override다. */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HudProperty(
    val nameKey: String = "",
    val descriptionKey: String = "",
)
