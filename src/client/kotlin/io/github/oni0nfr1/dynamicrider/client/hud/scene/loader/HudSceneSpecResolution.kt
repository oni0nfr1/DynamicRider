package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import java.nio.file.Path

/** 최종 선택된 HUD 장면 명세의 출처와 fallback 여부를 나타낸다. */
enum class HudSceneSource {
    CUSTOM_CONFIG,
    RESOURCE,
    CUSTOM_FALLBACK,
}

/** Repository가 config와 resource 후보를 조회해 최종 장면 명세를 결정한 결과다. */
sealed interface HudSceneSpecResolution {
    /** 사용할 명세를 찾은 결과다. [diagnostics]에는 fallback 원인이 포함될 수 있다. */
    data class Resolved(
        val spec: HudSceneSpec,
        val sourcePath: Path,
        val source: HudSceneSource,
        val diagnostics: List<HudSceneLoadError> = emptyList(),
    ) : HudSceneSpecResolution

    /** config와 resource 후보가 모두 실패한 결과다. */
    data class Failed(
        val errors: List<HudSceneLoadError>,
    ) : HudSceneSpecResolution
}
