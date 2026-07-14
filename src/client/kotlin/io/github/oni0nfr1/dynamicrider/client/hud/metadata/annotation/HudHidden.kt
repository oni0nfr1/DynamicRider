package io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/** 직렬화는 유지하되 일반 속성 패널에서는 숨길 속성임을 표시한다. */
@OptIn(ExperimentalSerializationApi::class)
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HudHidden
