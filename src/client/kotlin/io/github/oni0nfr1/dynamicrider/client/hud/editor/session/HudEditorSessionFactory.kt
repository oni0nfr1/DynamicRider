package io.github.oni0nfr1.dynamicrider.client.hud.editor.session

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.HudCommandStack
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneContextFactory
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneSynchronizer
import io.github.oni0nfr1.dynamicrider.client.hud.editor.service.HudSpecEditService
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneLoadError
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneRepository
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSource
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSpecResolution
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType

/** Repository가 선택한 장면 명세를 완전한 GUI editor session으로 조립한다. */
class HudEditorSessionFactory(
    private val repository: HudSceneRepository,
) {
    /**
     * [mode]와 [stateType]에 적용할 명세를 resolve하고 기본 preview context로 session을 연다.
     *
     * 이 함수는 기존 session을 닫거나 custom 파일을 생성하지 않는다.
     */
    fun open(
        mode: HudSceneMode,
        stateType: KartStateType<out KartState>,
        viewport: ElementHolder? = null,
    ): HudEditorSessionOpenResult {
        return when (val resolution = repository.resolveSpec(mode, stateType)) {
            is HudSceneSpecResolution.Failed -> HudEditorSessionOpenResult.ResolveFailed(resolution.errors)
            is HudSceneSpecResolution.Resolved -> runCatching {
                createResolved(
                    mode = mode,
                    source = resolution.source,
                    spec = resolution.spec,
                    previewContext = PreviewHudSceneContextFactory.create(stateType),
                    viewport = viewport,
                    diagnostics = resolution.diagnostics,
                )
            }.fold(
                onSuccess = HudEditorSessionOpenResult::Opened,
                onFailure = {
                    HudEditorSessionOpenResult.PreviewCreationFailed(
                        source = resolution.source,
                        diagnostics = resolution.diagnostics,
                        cause = it,
                    )
                },
            )
        }
    }

    private fun <S : KartState> createResolved(
        mode: HudSceneMode,
        source: HudSceneSource,
        spec: HudSceneSpec,
        previewContext: PreviewHudSceneContext<S>,
        viewport: ElementHolder?,
        diagnostics: List<HudSceneLoadError>,
    ): HudEditorSession<S> {
        val document = HudSceneDocument.from(spec)
        val commandStack = HudCommandStack(document)
        val scene = HudScene(previewContext, viewport)
        val synchronizer = PreviewHudSceneSynchronizer(document, commandStack, scene)
        return HudEditorSession(
            mode = mode,
            source = source,
            previewContext = previewContext,
            previewScene = scene,
            diagnostics = diagnostics,
            document = document,
            commandStack = commandStack,
            editService = HudSpecEditService(document),
            synchronizer = synchronizer,
            repository = repository,
        )
    }
}
