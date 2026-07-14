package io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/** 정수로 직렬화되는 ARGB/RGB 색상 속성임을 표시한다. */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HudColor(
    val alpha: Boolean = true,
)
