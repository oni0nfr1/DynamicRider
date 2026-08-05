package io.github.oni0nfr1.dynamicrider.client.hud.editor.session

import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentElement
import io.github.oni0nfr1.dynamicrider.client.hud.editor.hierarchy.HudHierarchyModel
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneLoadError
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSource
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType

/** GUI가 한 시점에 읽는 HUD editor session 상태 snapshot이다. */
data class HudEditorState(
    val mode: HudSceneMode,
    val kartStateType: KartStateType<out KartState>,
    val source: HudSceneSource,
    val elements: List<HudDocumentElement>,
    val hierarchy: HudHierarchyModel,
    val selectedPath: HudPath?,
    val dirty: Boolean,
    val canUndo: Boolean,
    val canRedo: Boolean,
    val diagnostics: List<HudSceneLoadError>,
    val previewFailure: Throwable?,
    val closed: Boolean,
) {
    /** Root 요소 전용 기존 조작에서 사용할 선택 root ID다. */
    val selectedElementId: String?
        get() = selectedPath?.firstSegment
}
