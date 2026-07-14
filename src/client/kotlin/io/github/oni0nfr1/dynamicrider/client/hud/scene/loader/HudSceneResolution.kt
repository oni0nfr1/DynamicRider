package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.skid.client.api.engine.KartEngine

/** 최종 선택된 HUD 장면의 출처와 fallback 여부를 나타낸다. */
enum class HudSceneSource {
    CUSTOM_CONFIG,
    RESOURCE,
    CUSTOM_FALLBACK,
}

/** repository의 장면 조회 결과다. */
sealed interface HudSceneResolution<out E : KartEngine> {
    /** 사용할 장면을 찾은 결과다. [diagnostics]에는 fallback 원인이 포함될 수 있다. */
    data class Resolved<E : KartEngine>(
        val scene: HudScene<E>,
        val source: HudSceneSource,
        val diagnostics: List<HudSceneLoadError> = emptyList(),
    ) : HudSceneResolution<E>

    /** config와 리소스 후보가 모두 실패한 결과다. */
    data class Failed(
        val errors: List<HudSceneLoadError>,
    ) : HudSceneResolution<Nothing>
}
