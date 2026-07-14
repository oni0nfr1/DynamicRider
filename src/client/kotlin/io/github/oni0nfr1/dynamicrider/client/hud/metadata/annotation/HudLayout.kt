package io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/** 중첩된 HUD 레이아웃 편집기로 다룰 속성임을 표시한다. */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HudLayout
