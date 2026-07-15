package io.github.oni0nfr1.dynamicrider.client.hud.editor.session

import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneLoadError
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSource
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState

/** Repository resolve부터 preview runtime 조립까지 포함한 editor session open 결과다. */
sealed interface HudEditorSessionOpenResult {
    /** Repository resolve와 preview 조립이 모두 완료되었다. */
    data class Opened(
        val session: HudEditorSession<out KartState>,
    ) : HudEditorSessionOpenResult

    /** 사용할 custom 또는 resource 장면 명세를 찾지 못했다. */
    data class ResolveFailed(
        val errors: List<HudSceneLoadError>,
    ) : HudEditorSessionOpenResult

    /** 명세는 resolve했지만 preview context 또는 runtime scene을 생성하지 못했다. */
    data class PreviewCreationFailed(
        val source: HudSceneSource,
        val diagnostics: List<HudSceneLoadError>,
        val cause: Throwable,
    ) : HudEditorSessionOpenResult
}
