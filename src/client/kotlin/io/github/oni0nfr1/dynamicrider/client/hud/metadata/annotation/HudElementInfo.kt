package io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/** HUD 요소의 팔레트 분류와 선택적 번역 key override다. */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HudElementInfo(
    val category: String,
    val nameKey: String = "",
    val icon: String = "",
)
