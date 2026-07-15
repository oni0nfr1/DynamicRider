package io.github.oni0nfr1.dynamicrider.client.hud.editor.session

import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneLoadError
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSource
import java.nio.file.Path

/** GUI가 요청한 custom 저장 또는 resource 복원 작업의 결과다. */
sealed interface HudEditorPersistenceResult {
    data class Saved(
        val path: Path,
    ) : HudEditorPersistenceResult

    data class Restored(
        val source: HudSceneSource,
        val customDeleted: Boolean,
    ) : HudEditorPersistenceResult

    /** 저장되지 않은 편집 내용을 버릴지 GUI 확인이 필요하다. */
    data object DiscardConfirmationRequired : HudEditorPersistenceResult

    /** Custom 삭제 후 사용할 resource 장면을 결정하지 못했다. */
    data class ResolveFailed(
        val errors: List<HudSceneLoadError>,
    ) : HudEditorPersistenceResult

    data class IoFailed(
        val operation: Operation,
        val cause: Throwable,
    ) : HudEditorPersistenceResult

    data object Closed : HudEditorPersistenceResult

    enum class Operation {
        SAVE,
        DELETE,
    }
}
