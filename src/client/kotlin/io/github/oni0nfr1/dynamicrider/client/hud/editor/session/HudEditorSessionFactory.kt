package io.github.oni0nfr1.dynamicrider.client.hud.editor.session

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.HudCommandStack
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneContextFactory
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneSynchronizer
import io.github.oni0nfr1.dynamicrider.client.hud.editor.service.HudSpecEditService
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneElementFactory
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneLoadError
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSource
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType

/** 이미 resolve되고 검증된 장면 입력으로 GUI editor session을 조립한다. */
object HudEditorSessionFactory {
    /** [stateType]의 기본 preview context까지 생성해 editor session을 조립한다. */
    fun create(
        mode: HudSceneMode,
        source: HudSceneSource,
        spec: HudSceneSpec,
        stateType: KartStateType<out KartState>,
        viewport: ElementHolder? = null,
        diagnostics: List<HudSceneLoadError> = emptyList(),
    ): Result<HudEditorSession<out KartState>> = createWithContext(
        mode = mode,
        source = source,
        spec = spec,
        previewContext = PreviewHudSceneContextFactory.create(stateType),
        viewport = viewport,
        diagnostics = diagnostics,
    )

    /**
     * [spec]의 작업 document, command stack과 preview runtime projection을 생성한다.
     *
     * Runtime 요소 생성 실패는 [Result.failure]로 반환한다.
     */
    fun <S : KartState> create(
        mode: HudSceneMode,
        source: HudSceneSource,
        spec: HudSceneSpec,
        previewContext: PreviewHudSceneContext<S>,
        viewport: ElementHolder? = null,
        diagnostics: List<HudSceneLoadError> = emptyList(),
        elementFactory: HudSceneElementFactory<S>? = null,
    ): Result<HudEditorSession<S>> = createWithContext(
        mode = mode,
        source = source,
        spec = spec,
        previewContext = previewContext,
        viewport = viewport,
        diagnostics = diagnostics,
        elementFactory = elementFactory,
    )

    private fun <S : KartState> createWithContext(
        mode: HudSceneMode,
        source: HudSceneSource,
        spec: HudSceneSpec,
        previewContext: PreviewHudSceneContext<S>,
        viewport: ElementHolder?,
        diagnostics: List<HudSceneLoadError>,
        elementFactory: HudSceneElementFactory<S>? = null,
    ): Result<HudEditorSession<S>> = runCatching {
        val document = HudSceneDocument.from(spec)
        val commandStack = HudCommandStack(document)
        val scene = if (elementFactory == null) {
            HudScene(previewContext, viewport)
        } else {
            HudScene(previewContext, viewport, elementFactory)
        }
        val synchronizer = PreviewHudSceneSynchronizer(document, commandStack, scene)
        HudEditorSession(
            mode = mode,
            source = source,
            previewContext = previewContext,
            previewScene = scene,
            diagnostics = diagnostics,
            document = document,
            commandStack = commandStack,
            editService = HudSpecEditService(document),
            synchronizer = synchronizer,
        )
    }
}
