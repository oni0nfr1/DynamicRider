package io.github.oni0nfr1.dynamicrider.client.hud.editor.session

import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.AddElementCommand
import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.HudCommandStack
import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.MoveElementCommand
import io.github.oni0nfr1.dynamicrider.client.hud.editor.command.RemoveElementCommand
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentChange
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudDocumentElement
import io.github.oni0nfr1.dynamicrider.client.hud.editor.document.HudSceneDocument
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudElementInspectionResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudElementInspector
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudElementPaletteEntry
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneSynchronizer
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.editor.service.HudSpecEditCommandResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.service.HudSpecEditService
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneLoadError
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSource
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidationResult
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidator
import kotlinx.serialization.json.JsonElement

/**
 * GUI 요청을 document command로 변환하고 하나의 preview scene에 반영하는 편집 수명주기다.
 *
 * GUI는 [state] snapshot과 이 클래스의 작업 함수만 사용하며 document와 command stack을 직접 변경하지 않는다.
 */
class HudEditorSession<S : KartState> internal constructor(
    val mode: HudSceneMode,
    val source: HudSceneSource,
    val previewContext: PreviewHudSceneContext<S>,
    val previewScene: HudScene<S>,
    diagnostics: List<HudSceneLoadError>,
    private val document: HudSceneDocument,
    private val commandStack: HudCommandStack,
    private val editService: HudSpecEditService,
    private val synchronizer: PreviewHudSceneSynchronizer<S>,
) : AutoCloseable {
    private val stateListeners = LinkedHashSet<(HudEditorState) -> Unit>()
    private val documentSubscription: AutoCloseable
    private var selectedElementId: String? = null
    private var closed: Boolean = false
    private val diagnostics: List<HudSceneLoadError> = diagnostics.toList()
    private var nextElementNumber: Int = 1
    private val elementInspector = HudElementInspector(document)

    /** 현재 document, selection, history 및 preview 상태의 snapshot이다. */
    val state: HudEditorState
        get() = HudEditorState(
            mode = mode,
            kartStateType = previewContext.kartStateType,
            source = source,
            elements = document.elements,
            selectedElementId = selectedElementId,
            dirty = document.dirty,
            canUndo = commandStack.canUndo,
            canRedo = commandStack.canRedo,
            diagnostics = diagnostics,
            previewFailure = synchronizer.lastFailure,
            closed = closed,
        )

    init {
        documentSubscription = commandStack.addChangeListener(::onDocumentChange)
    }

    /** 상태 변경 listener를 등록하고 요청하면 현재 snapshot을 즉시 전달한다. */
    fun addStateListener(
        emitCurrent: Boolean = true,
        listener: (HudEditorState) -> Unit,
    ): AutoCloseable {
        if (closed) {
            if (emitCurrent) listener(state)
            return AutoCloseable {}
        }
        stateListeners += listener
        if (emitCurrent) listener(state)
        return AutoCloseable { stateListeners -= listener }
    }

    /** 요소를 선택하거나 `null`로 현재 선택을 해제한다. */
    fun selectElement(elementId: String?): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        if (elementId != null && document.elementById(elementId) == null) {
            return HudEditorActionResult.ElementNotFound(elementId)
        }
        if (selectedElementId == elementId) return HudEditorActionResult.Unchanged
        selectedElementId = elementId
        publishState()
        return HudEditorActionResult.Applied(elementId)
    }

    /** [elementId]의 현재 Spec 값과 편집 metadata를 결합한 속성 패널 모델을 반환한다. */
    fun inspectElement(elementId: String): HudElementInspectionResult = elementInspector.inspect(elementId)

    /** 현재 카트 상태 타입과 호환되는 요소 팔레트의 읽기 전용 snapshot을 반환한다. */
    fun availableElementTypes(): List<HudElementPaletteEntry> =
        HudElementTypeRegistry.compatibleWith(previewContext.kartStateType).map { type ->
            val metadata = type.metadata
            HudElementPaletteEntry(
                typeId = type.id,
                nameKey = metadata.nameKey,
                category = metadata.category,
                categoryNameKey = metadata.categoryNameKey,
                icon = metadata.icon,
            )
        }

    /** Registry의 [typeId]에 대응하는 기본 Spec을 생성해 [index]에 추가하고 선택한다. */
    fun addElement(
        typeId: String,
        index: Int = Int.MAX_VALUE,
    ): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        val type = HudElementTypeRegistry.byId(typeId)
            ?: return HudEditorActionResult.ElementTypeNotFound(typeId)
        return addElement(type.createDefaultSpec(), index)
    }

    /** 검증된 [spec]을 새 영속 ID와 함께 [index]에 추가하고 선택한다. */
    fun addElement(
        spec: HudElementSpec<*, *>,
        index: Int = Int.MAX_VALUE,
    ): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        validateSpec(spec)?.let { return it }
        val elementId = generateElementId()
        selectedElementId = elementId
        commandStack.execute(AddElementCommand(HudDocumentElement(elementId, spec), index))
        return HudEditorActionResult.Applied(elementId)
    }

    /** [elementId] 요소를 제거한다. 선택 요소라면 인접 요소로 selection을 옮긴다. */
    fun removeElement(elementId: String): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        if (document.elementById(elementId) == null) return HudEditorActionResult.ElementNotFound(elementId)
        commandStack.execute(RemoveElementCommand(elementId))
        return HudEditorActionResult.Applied(elementId)
    }

    /** [elementId] 요소를 [targetIndex]로 이동한다. */
    fun moveElement(elementId: String, targetIndex: Int): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        val sourceIndex = document.elements.indexOfFirst { it.id == elementId }
        if (sourceIndex < 0) return HudEditorActionResult.ElementNotFound(elementId)
        val actualTarget = targetIndex.coerceIn(0, (document.elements.size - 1).coerceAtLeast(0))
        if (sourceIndex == actualTarget) return HudEditorActionResult.Unchanged
        commandStack.execute(MoveElementCommand(elementId, actualTarget))
        return HudEditorActionResult.Applied(elementId)
    }

    /** Spec property 변경을 검증된 command로 변환해 즉시 실행한다. */
    fun updateProperty(
        elementId: String,
        path: HudPropertyPath,
        value: JsonElement,
    ): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        return when (val result = editService.createPropertyChangeCommand(elementId, path, value)) {
            is HudSpecEditCommandResult.Created -> {
                commandStack.execute(result.command)
                HudEditorActionResult.Applied(elementId)
            }
            is HudSpecEditCommandResult.Unchanged -> HudEditorActionResult.Unchanged
            is HudSpecEditCommandResult.ElementNotFound -> HudEditorActionResult.ElementNotFound(result.elementId)
            is HudSpecEditCommandResult.PropertyRejected -> HudEditorActionResult.PropertyRejected(result.failure)
        }
    }

    /** 마지막 편집 command를 되돌린다. */
    fun undo(): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        return if (commandStack.undo()) HudEditorActionResult.Applied() else HudEditorActionResult.Unchanged
    }

    /** 마지막으로 되돌린 편집 command를 다시 적용한다. */
    fun redo(): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        return if (commandStack.redo()) HudEditorActionResult.Applied() else HudEditorActionResult.Unchanged
    }

    override fun close() {
        if (closed) return
        documentSubscription.close()
        synchronizer.close()
        closed = true
        publishState()
        stateListeners.clear()
    }

    private fun onDocumentChange(change: HudDocumentChange) {
        if (change is HudDocumentChange.Removed && selectedElementId == change.element.id) {
            val elements = document.elements
            selectedElementId = elements.getOrNull(change.index.coerceAtMost(elements.lastIndex))?.id
        }
        publishState()
    }

    private fun validateSpec(spec: HudElementSpec<*, *>): HudEditorActionResult? {
        val stateClass = previewContext.kartStateType.stateClass
        val requiredStateClass = spec.requiredStateClass()
        if (!requiredStateClass.isAssignableFrom(stateClass)) {
            return HudEditorActionResult.IncompatibleState(
                requiredStateClass = requiredStateClass,
                sceneStateClass = stateClass,
                specType = spec::class.java.name,
            )
        }
        return when (val validation = HudSpecValidator.validate(spec)) {
            HudSpecValidationResult.Valid -> null
            is HudSpecValidationResult.Invalid -> HudEditorActionResult.InvalidSpec(validation.errors)
        }
    }

    private fun generateElementId(): String {
        while (true) {
            val candidate = "element-${nextElementNumber++}"
            if (document.elementById(candidate) == null) return candidate
        }
    }

    private fun publishState() {
        val snapshot = state
        stateListeners.toList().forEach { it(snapshot) }
    }
}
