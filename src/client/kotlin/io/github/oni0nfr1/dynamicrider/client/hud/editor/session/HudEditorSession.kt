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
import io.github.oni0nfr1.dynamicrider.client.hud.editor.hierarchy.HudHierarchyBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.editor.hierarchy.HudHierarchyNodeKind
import io.github.oni0nfr1.dynamicrider.client.hud.editor.hierarchy.HudHierarchyModel
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewHudSceneSynchronizer
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewStateField
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewStateFieldEditor
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewStatePreset
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewStatePresetApplier
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewStatePresets
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewStateUpdateResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPath
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudSpecPropertyUpdateResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.service.HudSpecEditCommandResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.service.HudSpecEditService
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementType
import io.github.oni0nfr1.dynamicrider.client.hud.elements.registry.HudElementTypeRegistry
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneLoadError
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneRepository
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSource
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSpecResolution
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidationResult
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/**
 * GUI 요청을 document command로 변환하고 하나의 preview scene에 반영하는 편집 수명주기다.
 *
 * GUI는 [state] snapshot과 이 클래스의 작업 함수만 사용하며 document와 command stack을 직접 변경하지 않는다.
 */
class HudEditorSession<S : KartState> internal constructor(
    val mode: HudSceneMode,
    source: HudSceneSource,
    val previewContext: PreviewHudSceneContext<S>,
    val previewScene: HudScene<S>,
    diagnostics: List<HudSceneLoadError>,
    private val document: HudSceneDocument,
    private val commandStack: HudCommandStack,
    private val editService: HudSpecEditService,
    private val synchronizer: PreviewHudSceneSynchronizer<S>,
    private val repository: HudSceneRepository,
) : AutoCloseable {
    private val stateListeners = LinkedHashSet<(HudEditorState) -> Unit>()
    private val documentSubscription: AutoCloseable
    private var hierarchy: HudHierarchyModel = HudHierarchyBuilder(document).build()
    private var selectedPath: HudPath? = null
    private var closed: Boolean = false
    private var currentSource: HudSceneSource = source
    private var currentDiagnostics: List<HudSceneLoadError> = diagnostics.toList()
    private var nextElementNumber: Int = 1
    private val elementInspector = HudElementInspector(document)

    /** 현재 document가 유래한 custom 또는 resource 출처다. */
    val source: HudSceneSource
        get() = currentSource

    /** 현재 document, selection, history 및 preview 상태의 snapshot이다. */
    val state: HudEditorState
        get() = HudEditorState(
            mode = mode,
            kartStateType = previewContext.kartStateType,
            source = source,
            elements = document.elements,
            hierarchy = hierarchy,
            selectedPath = selectedPath,
            dirty = document.dirty,
            canUndo = commandStack.canUndo,
            canRedo = commandStack.canRedo,
            diagnostics = currentDiagnostics,
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
        return select(elementId?.let { HudPath.of(it) })
    }

    /** Root 또는 중첩 요소 [path]를 선택하며 document와 command history는 변경하지 않는다. */
    fun select(path: HudPath?): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        if (path != null && path !in hierarchy) {
            return HudEditorActionResult.ElementNotFound(path.toString())
        }
        if (selectedPath == path) return HudEditorActionResult.Unchanged
        selectedPath = path
        publishState()
        return HudEditorActionResult.Applied(path?.firstSegment)
    }

    /** [elementId]의 현재 Spec 값과 편집 metadata를 결합한 속성 패널 모델을 반환한다. */
    fun inspectElement(elementId: String): HudElementInspectionResult = elementInspector.inspect(elementId)

    /** 절대 [path]의 root 또는 중첩 요소에 대한 Inspector snapshot을 반환한다. */
    fun inspect(path: HudPath): HudElementInspectionResult = elementInspector.inspect(path)

    /** 현재 카트 상태 타입과 호환되는 요소 팔레트의 읽기 전용 snapshot을 반환한다. */
    fun availableElementTypes(): List<HudElementPaletteEntry> =
        HudElementTypeRegistry.compatibleWith(previewContext.kartStateType)
            .filter(HudElementType<*, *>::visibleInEditor)
            .map { type ->
                val metadata = type.metadata
                HudElementPaletteEntry(
                    typeId = type.id,
                    nameKey = metadata.nameKey,
                    category = metadata.category,
                    categoryNameKey = metadata.categoryNameKey,
                    icon = metadata.icon,
                )
            }

    /** 현재 preview 카트 capability에 맞는 built-in 상태 preset을 반환한다. */
    fun availablePreviewPresets(): List<PreviewStatePreset> =
        PreviewStatePresets.compatibleWith(previewContext.kartState)

    /** 현재 preview context에서 직접 조절할 수 있는 상태 필드 snapshot을 반환한다. */
    fun previewStateFields(): List<PreviewStateField> = PreviewStateFieldEditor.fields(previewContext)

    /** Built-in preview [presetId]를 현재 context에 적용하며 document와 command history는 변경하지 않는다. */
    fun applyPreviewPreset(presetId: String): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        val preset = PreviewStatePresets.byId(presetId)
            ?: return HudEditorActionResult.PreviewPresetNotFound(presetId)
        if (!preset.isCompatibleWith(previewContext.kartState)) {
            return HudEditorActionResult.PreviewStateRejected(presetId)
        }
        PreviewStatePresetApplier.apply(previewContext, preset)
        publishState()
        return HudEditorActionResult.Applied()
    }

    /** Preview [fieldId]를 변경하며 영속 Spec, dirty 및 undo/redo 기록에는 영향을 주지 않는다. */
    fun updatePreviewState(fieldId: String, value: JsonElement): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        return when (PreviewStateFieldEditor.update(previewContext, fieldId, value)) {
            PreviewStateUpdateResult.Updated -> {
                publishState()
                HudEditorActionResult.Applied()
            }
            PreviewStateUpdateResult.Unchanged -> HudEditorActionResult.Unchanged
            is PreviewStateUpdateResult.FieldNotFound,
            is PreviewStateUpdateResult.InvalidValue,
            -> HudEditorActionResult.PreviewStateRejected(fieldId)
        }
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
        selectedPath = HudPath.of(elementId)
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
        path: HudPath,
        value: JsonElement,
        mergeWithPrevious: Boolean = false,
    ): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        return when (val result = editService.createPropertyChangeCommand(elementId, path, value)) {
            is HudSpecEditCommandResult.Created -> {
                commandStack.execute(result.command, mergeWithPrevious)
                HudEditorActionResult.Applied(elementId)
            }
            is HudSpecEditCommandResult.Unchanged -> HudEditorActionResult.Unchanged
            is HudSpecEditCommandResult.ElementNotFound -> HudEditorActionResult.ElementNotFound(result.elementId)
            is HudSpecEditCommandResult.PropertyRejected -> HudEditorActionResult.PropertyRejected(result.failure)
        }
    }

    /** 같은 root 요소에 속한 하나 이상의 primitive leaf를 하나의 undo/redo 작업으로 변경한다. */
    fun updateLeaf(
        vararg changes: Pair<HudPath, JsonPrimitive>,
        mergeWithPrevious: Boolean = false,
    ): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        require(changes.isNotEmpty()) { "At least one HUD leaf change is required" }
        val rootId = changes.first().first.firstSegment
        require(changes.all { it.first.firstSegment == rootId }) {
            "A HUD leaf update cannot span multiple root elements"
        }
        val relativeChanges = changes.map { (path, value) ->
            if (path.isSingle) {
                return HudEditorActionResult.PropertyRejected(
                    HudSpecPropertyUpdateResult.Failure(
                        path = path,
                        reason = HudSpecPropertyUpdateResult.Reason.UNKNOWN_PROPERTY,
                        message = "HUD leaf path '$path' does not contain a property",
                    ),
                )
            }
            HudPath.of(*path.segments.drop(1).toTypedArray()) to value
        }
        return when (val result = editService.createLeafChangeCommand(rootId, *relativeChanges.toTypedArray())) {
            is HudSpecEditCommandResult.Created -> {
                commandStack.execute(result.command, mergeWithPrevious)
                HudEditorActionResult.Applied(rootId)
            }
            is HudSpecEditCommandResult.Unchanged -> HudEditorActionResult.Unchanged
            is HudSpecEditCommandResult.ElementNotFound -> HudEditorActionResult.ElementNotFound(result.elementId)
            is HudSpecEditCommandResult.PropertyRejected -> HudEditorActionResult.PropertyRejected(result.failure)
        }
    }

    /** sealed property를 지정 subtype으로 교체하고 새 Spec을 undo/redo history에 적용한다. */
    fun changePropertyVariant(
        elementId: String,
        path: HudPath,
        variantSerialName: String,
    ): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        return when (val result = editService.createVariantChangeCommand(elementId, path, variantSerialName)) {
            is HudSpecEditCommandResult.Created -> {
                commandStack.execute(result.command)
                HudEditorActionResult.Applied(elementId)
            }
            is HudSpecEditCommandResult.Unchanged -> HudEditorActionResult.Unchanged
            is HudSpecEditCommandResult.ElementNotFound -> HudEditorActionResult.ElementNotFound(result.elementId)
            is HudSpecEditCommandResult.PropertyRejected -> HudEditorActionResult.PropertyRejected(result.failure)
        }
    }

    /** 절대 [path]의 일반 또는 요소 sealed property를 지정 subtype으로 교체한다. */
    fun changeVariant(
        path: HudPath,
        variantSerialName: String,
    ): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        val (rootId, relativePath) = splitPropertyPath(path) ?: return missingPropertyPath(path)
        val hierarchyKind = hierarchy[path]?.kind as? HudHierarchyNodeKind.Variant
        if (hierarchyKind != null) {
            val variant = hierarchyKind.variants.firstOrNull { it.typeId == variantSerialName }
                ?: return rejected(path, "Unknown HUD element variant '$variantSerialName'")
            val type = HudElementTypeRegistry.byId(variant.typeId)
                ?: return HudEditorActionResult.ElementTypeNotFound(variant.typeId)
            if (!type.accepts(previewContext.kartStateType)) {
                return HudEditorActionResult.IncompatibleState(
                    requiredStateClass = type.requiredStateClass,
                    sceneStateClass = previewContext.kartStateType.stateClass,
                    specType = type.specClass.java.name,
                )
            }
        }
        return applyEditResult(rootId, editService.createVariantChangeCommand(rootId, relativePath, variantSerialName))
    }

    /** 절대 [path]의 nullable property를 비활성화하거나 스키마 기본값으로 활성화한다. */
    fun setPresence(path: HudPath, present: Boolean): HudEditorActionResult {
        if (closed) return HudEditorActionResult.Closed
        val (rootId, relativePath) = splitPropertyPath(path) ?: return missingPropertyPath(path)
        if (present) {
            when (val kind = hierarchy[path]?.kind) {
                is HudHierarchyNodeKind.Fixed -> compatibilityFailure(kind.typeId)?.let { return it }
                is HudHierarchyNodeKind.Variant -> kind.variants.firstOrNull()?.typeId
                    ?.let(::compatibilityFailure)
                    ?.let { return it }
                else -> Unit
            }
        }
        return applyEditResult(rootId, editService.createPresenceChangeCommand(rootId, relativePath, present))
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

    /** 현재 document를 config override로 저장하고 성공한 snapshot을 clean 기준점으로 표시한다. */
    fun save(): HudEditorPersistenceResult {
        if (closed) return HudEditorPersistenceResult.Closed
        return repository.saveCustom(mode, previewContext.kartStateType, document.toSpec()).fold(
            onSuccess = { path ->
                document.markClean()
                currentSource = HudSceneSource.CUSTOM_CONFIG
                currentDiagnostics = emptyList()
                publishState()
                HudEditorPersistenceResult.Saved(path)
            },
            onFailure = { cause ->
                HudEditorPersistenceResult.IoFailed(
                    operation = HudEditorPersistenceResult.Operation.SAVE,
                    cause = cause,
                )
            },
        )
    }

    /**
     * Config override를 삭제하고 repository가 다시 선택한 resource 장면으로 작업 사본을 복원한다.
     *
     * 저장되지 않은 변경이 있으면 [discardUnsavedChanges]가 `true`일 때만 폐기한다.
     * 현재 live HUD에는 복원 결과를 자동으로 적용하지 않는다.
     */
    fun deleteCustom(discardUnsavedChanges: Boolean = false): HudEditorPersistenceResult {
        if (closed) return HudEditorPersistenceResult.Closed
        if (document.dirty && !discardUnsavedChanges) {
            return HudEditorPersistenceResult.DiscardConfirmationRequired
        }
        val customDeleted = repository.deleteCustom(mode, previewContext.kartStateType).fold(
            onSuccess = { it },
            onFailure = { cause ->
                return HudEditorPersistenceResult.IoFailed(
                    operation = HudEditorPersistenceResult.Operation.DELETE,
                    cause = cause,
                )
            },
        )
        return when (val resolution = repository.resolveSpec(mode, previewContext.kartStateType)) {
            is HudSceneSpecResolution.Failed -> HudEditorPersistenceResult.ResolveFailed(resolution.errors)
            is HudSceneSpecResolution.Resolved -> {
                currentSource = resolution.source
                currentDiagnostics = resolution.diagnostics.toList()
                commandStack.reset(resolution.spec)
                HudEditorPersistenceResult.Restored(
                    source = resolution.source,
                    customDeleted = customDeleted,
                )
            }
        }
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
        val oldHierarchy = hierarchy
        hierarchy = HudHierarchyBuilder(document).build()
        selectedPath = reconcileSelection(change, oldHierarchy, hierarchy, selectedPath)
        publishState()
    }

    private fun reconcileSelection(
        change: HudDocumentChange,
        oldHierarchy: HudHierarchyModel,
        newHierarchy: HudHierarchyModel,
        selected: HudPath?,
    ): HudPath? {
        selected ?: return null
        if (change is HudDocumentChange.Removed && selected.firstSegment == change.element.id) {
            return newHierarchy.roots
                .getOrNull(change.index.coerceAtMost(newHierarchy.roots.lastIndex))
                ?.path
        }

        for (size in 1..selected.segments.size) {
            val prefix = HudPath.of(*selected.segments.take(size).toTypedArray())
            val oldType = oldHierarchy[prefix]?.kind?.selectedTypeId
            val newType = newHierarchy[prefix]?.kind?.selectedTypeId
            if (oldType != null && oldType != newType) {
                return prefix.takeIf { it in newHierarchy }
            }
        }
        var candidate: HudPath? = selected
        while (candidate != null) {
            if (candidate in newHierarchy) return candidate
            candidate = candidate.parent
        }
        return if (change is HudDocumentChange.Reset) newHierarchy.roots.firstOrNull()?.path else null
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

    private fun splitPropertyPath(path: HudPath): Pair<String, HudPath>? =
        path.segments.drop(1)
            .takeIf(List<String>::isNotEmpty)
            ?.let { path.firstSegment to HudPath.of(*it.toTypedArray()) }

    private fun missingPropertyPath(path: HudPath): HudEditorActionResult.PropertyRejected = rejected(
        path,
        "HUD path '$path' does not contain a property",
        HudSpecPropertyUpdateResult.Reason.UNKNOWN_PROPERTY,
    )

    private fun rejected(
        path: HudPath,
        message: String,
        reason: HudSpecPropertyUpdateResult.Reason = HudSpecPropertyUpdateResult.Reason.INVALID_VALUE,
    ): HudEditorActionResult.PropertyRejected = HudEditorActionResult.PropertyRejected(
        HudSpecPropertyUpdateResult.Failure(path, reason, message),
    )

    private fun compatibilityFailure(typeId: String): HudEditorActionResult? {
        val type = HudElementTypeRegistry.byId(typeId)
            ?: return HudEditorActionResult.ElementTypeNotFound(typeId)
        return if (type.accepts(previewContext.kartStateType)) {
            null
        } else {
            HudEditorActionResult.IncompatibleState(
                requiredStateClass = type.requiredStateClass,
                sceneStateClass = previewContext.kartStateType.stateClass,
                specType = type.specClass.java.name,
            )
        }
    }

    private fun applyEditResult(
        rootId: String,
        result: HudSpecEditCommandResult,
    ): HudEditorActionResult = when (result) {
        is HudSpecEditCommandResult.Created -> {
            commandStack.execute(result.command)
            HudEditorActionResult.Applied(rootId)
        }
        is HudSpecEditCommandResult.Unchanged -> HudEditorActionResult.Unchanged
        is HudSpecEditCommandResult.ElementNotFound -> HudEditorActionResult.ElementNotFound(result.elementId)
        is HudSpecEditCommandResult.PropertyRejected -> HudEditorActionResult.PropertyRejected(result.failure)
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
