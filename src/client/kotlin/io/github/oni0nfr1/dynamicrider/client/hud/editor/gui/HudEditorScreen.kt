package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import io.github.oni0nfr1.dynamicrider.client.gui.widget.DropdownEntry
import io.github.oni0nfr1.dynamicrider.client.gui.widget.DropdownWidget
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudElementInspectionResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditableProperty
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditablePropertySchema
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.propertyAt
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.editor.preview.PreviewStateField
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorActionResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorPersistenceResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorSession
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorSessionFactory
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorSessionOpenResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorState
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudNumberType
import io.github.oni0nfr1.dynamicrider.client.hud.layout.HudLayoutEngine
import io.github.oni0nfr1.dynamicrider.client.graphics.render.DynRiderRenderTypes
import io.github.oni0nfr1.dynamicrider.client.graphics.render.batch
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateType
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartStateTypes
import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSource
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.CycleButton
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.ceil
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** 중앙 HUD preview와 탭식 단일 side panel을 제공하는 첫 editor 화면이다. */
class HudEditorScreen(
    private val parentScreen: Screen,
    private val sessionFactory: HudEditorSessionFactory,
    private val session: HudEditorSession<out KartState>,
    private val previewViewport: HudEditorPreviewViewport,
) : Screen(Component.translatable("dynamicrider.hud.editor.title")) {
    private enum class SideTab(val key: String) {
        ELEMENTS("dynamicrider.hud.editor.tab.elements"),
        PROPERTIES("dynamicrider.hud.editor.tab.properties"),
        PREVIEW_STATE("dynamicrider.hud.editor.tab.preview_state"),
    }

    private var activeTab = SideTab.ELEMENTS
    private var paletteOpen = false
    private var status: Component? = null
    private var stateSubscription: AutoCloseable? = null
    private var closing = false
    private var preferredSideWidth: Int? = null
    private var resizingSidePanel = false
    private var lastDividerClickMillis = 0L
    private var elementScroll = 0
    private var paletteScroll = 0
    private var propertyScroll = 0
    private var layoutScroll = 0
    private var previewStateScroll = 0
    private var selectedPreviewPresetId: String? = null
    private var previewPresetDropdown: DropdownWidget<String>? = null
    private var layoutEditorOpen = false
    private var sealedEditorPath: HudPropertyPath? = null
    private var sealedEditorScroll = 0
    private var sealedVariantDropdown: DropdownWidget<String>? = null
    private var elementDrag: ElementDrag? = null
    private var scaleDrag: ScaleDrag? = null
    private var modal: EditorModal? = null
    private var previewMode = false
    private var editorWidgets: List<AbstractWidget> = emptyList()
    private var modalWidgets: List<AbstractWidget> = emptyList()
    private val propertyErrors = mutableMapOf<Pair<String, HudPropertyPath>, String>()
    private val previewStateErrors = mutableMapOf<String, String>()

    private var previewX = 8
    private var previewY = 38
    private var previewWidth = 1
    private var previewHeight = 1
    private var sideX = 1
    private var sideWidth = 1
    private var previewTransform = HudPreviewTransform.fit(1, 1, 0f, 0f, 1f, 1f)

    override fun init() {
        calculateLayout()
        previewPresetDropdown = null
        sealedVariantDropdown = null
        if (stateSubscription == null) {
            stateSubscription = session.addStateListener(emitCurrent = false) {
                Minecraft.getInstance().execute {
                    if (Minecraft.getInstance().screen === this) {
                        if (propertyErrors.isNotEmpty()) {
                            propertyErrors.clear()
                            status = null
                        }
                        rebuildWidgets()
                    }
                }
            }
        }
        if (!previewMode) {
            buildToolbar()
            buildTabs()
            when (activeTab) {
                SideTab.ELEMENTS -> buildElementsTab()
                SideTab.PROPERTIES -> buildPropertiesTab()
                SideTab.PREVIEW_STATE -> buildPreviewStateTab()
            }
        }
        editorWidgets = children().filterIsInstance<AbstractWidget>()
        when (val currentModal = modal) {
            null -> Unit
            is EditorModal.ScenePicker -> buildScenePicker(currentModal)
            is EditorModal.ConfirmDeparture -> buildDepartureConfirmation(currentModal.action)
            EditorModal.ConfirmRestore -> buildRestoreConfirmation()
        }
        modalWidgets = if (modal == null) {
            emptyList()
        } else {
            children().filterIsInstance<AbstractWidget>().filterNot(editorWidgets::contains)
        }
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        guiGraphics.fill(0, 0, width, height, 0xF0080808.toInt())
        if (previewMode) {
            renderPreview(guiGraphics)
            renderPreviewModeHint(guiGraphics)
            return
        }
        renderEditorChrome(guiGraphics, mouseX, mouseY)
        renderPreview(guiGraphics)
        renderSideContent(guiGraphics)
        if (modal == null) {
            super.render(guiGraphics, mouseX, mouseY, partialTick)
            previewPresetDropdown?.renderPopup(guiGraphics, mouseX, mouseY)
            sealedVariantDropdown?.renderPopup(guiGraphics, mouseX, mouseY)
        } else {
            editorWidgets.forEach { it.render(guiGraphics, -1, -1, partialTick) }
            guiGraphics.flush()
            guiGraphics.pose().pushPose()
            guiGraphics.pose().translate(0f, 0f, MODAL_Z)
            renderModal(guiGraphics)
            modalWidgets.forEach { it.render(guiGraphics, mouseX, mouseY, partialTick) }
            guiGraphics.pose().popPose()
        }
    }

    /** Editor는 자체 배경을 그리므로 vanilla menu blur와 배경 texture를 적용하지 않는다. */
    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (previewMode) return super.mouseClicked(mouseX, mouseY, button)
        if (modal != null) return super.mouseClicked(mouseX, mouseY, button)
        listOfNotNull(previewPresetDropdown, sealedVariantDropdown).firstOrNull { it.isExpanded }?.let {
            if (it.mouseClicked(mouseX, mouseY, button)) return true
        }
        if (button == 0 && isOverDivider(mouseX, mouseY)) {
            val now = System.currentTimeMillis()
            if (now - lastDividerClickMillis <= DOUBLE_CLICK_MILLIS) {
                preferredSideWidth = null
                resizingSidePanel = false
                lastDividerClickMillis = 0L
                rebuildWidgets()
            } else {
                lastDividerClickMillis = now
                resizingSidePanel = true
                isDragging = true
            }
            return true
        }
        if (button == 0 && isInsidePreview(mouseX, mouseY)) {
            val logical = previewTransform.screenToLogical(mouseX, mouseY)
            val localX = logical.x
            val localY = logical.y
            val guides = previewGuides()
            val selectedGuide = session.state.selectedElementId
                ?.let { selectedId -> guides.firstOrNull { it.elementId == selectedId } }
            if (selectedGuide != null && isOverScaleHandle(selectedGuide, mouseX, mouseY)) {
                beginScaleDrag(selectedGuide)
                return true
            }
            val guide = HudPreviewElementGuideCalculator.hitTest(guides, localX, localY)
            session.selectElement(guide?.elementId)
            if (guide != null) {
                activeTab = SideTab.PROPERTIES
                paletteOpen = false
                propertyErrors.clear()
                elementDrag = ElementDrag(
                    elementId = guide.elementId,
                    grabOffsetX = localX - guide.elementAnchorX,
                    grabOffsetY = localY - guide.elementAnchorY,
                )
                isDragging = true
            }
            rebuildWidgets()
            return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        dragX: Double,
        dragY: Double,
    ): Boolean {
        if (previewMode) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
        if (modal != null) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
        if (resizingSidePanel && button == 0) {
            preferredSideWidth = width - SCREEN_PADDING - mouseX.toInt() - PANEL_INSET
            rebuildWidgets()
            return true
        }
        if (button == 0) {
            scaleDrag?.let { drag ->
                scaleSelectedElement(mouseX, mouseY, drag)
                return true
            }
            elementDrag?.let { drag ->
                dragSelectedElement(mouseX, mouseY, drag)
                return true
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (previewMode) return super.mouseReleased(mouseX, mouseY, button)
        if (modal != null) return super.mouseReleased(mouseX, mouseY, button)
        if (resizingSidePanel && button == 0) {
            resizingSidePanel = false
            isDragging = false
            return true
        }
        if (button == 0 && (elementDrag != null || scaleDrag != null)) {
            elementDrag = null
            scaleDrag = null
            isDragging = false
            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        scrollX: Double,
        scrollY: Double,
    ): Boolean {
        if (previewMode) return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
        val currentModal = modal
        if (currentModal is EditorModal.ScenePicker) {
            val direction = when {
                scrollY > 0.0 -> -1
                scrollY < 0.0 -> 1
                else -> 0
            }
            if (direction != 0) {
                currentModal.scroll = clampScroll(
                    currentModal.scroll + direction,
                    KartStateTypes.entries.size,
                    scenePickerVisibleRows(),
                )
                rebuildWidgets()
            }
            return true
        }
        if (currentModal != null) return true
        listOfNotNull(previewPresetDropdown, sealedVariantDropdown).firstOrNull { it.isExpanded }?.let {
            if (it.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true
        }
        if (mouseX < sideX || mouseY < previewY + TAB_HEIGHT || mouseY > height) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
        }
        val direction = when {
            scrollY > 0.0 -> -1
            scrollY < 0.0 -> 1
            else -> 0
        }
        if (direction == 0) return true
        when (activeTab) {
            SideTab.ELEMENTS -> if (paletteOpen) {
                paletteScroll = clampScroll(paletteScroll + direction, session.availableElementTypes().size, paletteVisibleRows())
            } else {
                elementScroll = clampScroll(elementScroll + direction, session.state.elements.size, elementVisibleRows())
            }
            SideTab.PROPERTIES -> {
                if (layoutEditorOpen) {
                    val count = selectedLayoutFields()?.size ?: 0
                    layoutScroll = clampScroll(layoutScroll + direction, count, layoutVisibleRows())
                } else if (sealedEditorPath != null) {
                    val count = selectedSealedProperties()?.size ?: 0
                    sealedEditorScroll = clampScroll(sealedEditorScroll + direction, count, sealedVisibleRows())
                } else {
                    val count = selectedProperties()?.size ?: 0
                    propertyScroll = clampScroll(propertyScroll + direction, count, propertyVisibleRows())
                }
            }
            SideTab.PREVIEW_STATE -> {
                val count = session.previewStateFields().size
                previewStateScroll = clampScroll(previewStateScroll + direction, count, previewStateVisibleRows())
            }
        }
        rebuildWidgets()
        return true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (previewMode) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                exitPreviewMode()
                return true
            }
            return super.keyPressed(keyCode, scanCode, modifiers)
        }
        if (modal != null) return super.keyPressed(keyCode, scanCode, modifiers)
        listOfNotNull(previewPresetDropdown, sealedVariantDropdown).firstOrNull { it.isExpanded }?.let {
            if (it.keyPressed(keyCode, scanCode, modifiers)) return true
        }
        if (Screen.hasControlDown()) {
            when (keyCode) {
                GLFW.GLFW_KEY_S -> {
                    val input = focused as? HudCommitEditBox
                    if (input == null || input.commitPending()) {
                        clearFocus()
                        save()
                    }
                    return true
                }
                GLFW.GLFW_KEY_Z -> {
                    if (Screen.hasShiftDown()) session.redo() else session.undo()
                    return true
                }
                GLFW.GLFW_KEY_Y -> {
                    session.redo()
                    return true
                }
            }
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE && focused !is EditBox) {
            removeSelected()
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun onClose() {
        if (previewMode) {
            exitPreviewMode()
            return
        }
        if (modal != null) {
            modal = null
            rebuildWidgets()
            return
        }
        requestDeparture(DepartureAction.Exit)
    }

    override fun removed() {
        stateSubscription?.close()
        stateSubscription = null
        session.close()
        super.removed()
    }

    override fun isPauseScreen(): Boolean = true

    private fun calculateLayout() {
        val defaultWidth = min(DEFAULT_SIDE_WIDTH, (width / 3).coerceAtLeast(MIN_SIDE_WIDTH))
        val desiredWidth = preferredSideWidth ?: defaultWidth
        val maximumByPreview = width - previewX - PANEL_GAP - SCREEN_PADDING - MIN_PREVIEW_WIDTH
        val maximumWidth = min(MAX_SIDE_WIDTH, maximumByPreview.coerceAtLeast(1))
        val minimumWidth = min(MIN_SIDE_WIDTH, maximumWidth)
        sideWidth = desiredWidth.coerceIn(minimumWidth, maximumWidth)
        sideX = width - sideWidth - SCREEN_PADDING
        previewWidth = (sideX - previewX - PANEL_GAP).coerceAtLeast(1)
        previewHeight = (height - previewY - SCREEN_PADDING).coerceAtLeast(1)
        previewTransform = if (previewMode) {
            previewViewport.resize(width, height)
            HudPreviewTransform.fit(width, height, 0f, 0f, width.toFloat(), height.toFloat())
        } else {
            previewViewport.resize(previewWidth, previewHeight)
            HudPreviewTransform.fit(
                previewWidth,
                previewHeight,
                previewX.toFloat(),
                previewY.toFloat(),
                previewWidth.toFloat(),
                previewHeight.toFloat(),
            )
        }
    }

    private fun buildToolbar() {
        val state = session.state
        addRenderableWidget(toolbarButton(8, "dynamicrider.hud.editor.undo") { session.undo() }.also {
            it.active = state.canUndo
        })
        addRenderableWidget(toolbarButton(72, "dynamicrider.hud.editor.redo") { session.redo() }.also {
            it.active = state.canRedo
        })
        addRenderableWidget(toolbarButton(136, "dynamicrider.hud.editor.preview_mode.enter") { enterPreviewMode() })
        addRenderableWidget(
            Button.builder(Component.translatable("dynamicrider.hud.editor.scene.change")) { openScenePicker() }
                .bounds(width / 2 + 36, 6, 84, 20)
                .build()
        )
        addRenderableWidget(toolbarButton(width - 206, "dynamicrider.hud.editor.save") { save() })
        addRenderableWidget(
            toolbarButton(width - 142, "dynamicrider.hud.editor.restore") { openRestoreConfirmation() }.also {
                it.active = state.source != HudSceneSource.RESOURCE || state.dirty
            }
        )
        addRenderableWidget(toolbarButton(width - 70, "gui.done") { requestDeparture(DepartureAction.Exit) })
    }

    private fun buildTabs() {
        val tabWidth = sideWidth / SideTab.entries.size
        SideTab.entries.forEachIndexed { index, tab ->
            addRenderableWidget(
                Button.builder(Component.translatable(tab.key)) {
                    activeTab = tab
                    paletteOpen = false
                    layoutEditorOpen = false
                    sealedEditorPath = null
                    rebuildWidgets()
                }.bounds(sideX + index * tabWidth, previewY, tabWidth, 20).build().also {
                    it.active = activeTab != tab
                }
            )
        }
    }

    private fun buildElementsTab() {
        val contentY = previewY + 26
        if (paletteOpen) {
            val entries = session.availableElementTypes()
            val visibleRows = paletteVisibleRows()
            paletteScroll = clampScroll(paletteScroll, entries.size, visibleRows)
            entries.drop(paletteScroll).take(visibleRows).forEachIndexed { index, entry ->
                addRenderableWidget(
                    Button.builder(Component.translatable(entry.nameKey)) {
                        activeTab = SideTab.PROPERTIES
                        paletteOpen = false
                        layoutEditorOpen = false
                        when (val result = session.addElement(entry.typeId)) {
                            is HudEditorActionResult.Applied -> {
                                propertyErrors.clear()
                            }
                            else -> {
                                activeTab = SideTab.ELEMENTS
                                paletteOpen = true
                                status = Component.translatable("dynamicrider.hud.editor.action_failed")
                            }
                        }
                        rebuildWidgets()
                    }.bounds(sideX, contentY + index * 22, sideWidth, 20).build()
                )
            }
            addRenderableWidget(
                Button.builder(Component.translatable("gui.back")) {
                    paletteOpen = false
                    rebuildWidgets()
                }.bounds(sideX, height - 28, sideWidth, 20).build()
            )
            return
        }

        val state = session.state
        val visibleRows = elementVisibleRows()
        elementScroll = clampScroll(elementScroll, state.elements.size, visibleRows)
        state.elements.drop(elementScroll).take(visibleRows).forEachIndexed { index, element ->
            val label = when (val inspected = session.inspectElement(element.id)) {
                is HudElementInspectionResult.Inspected -> Component.translatable(inspected.model.nameKey)
                else -> Component.literal(element.id)
            }
            addRenderableWidget(
                Button.builder(label) {
                    session.selectElement(element.id)
                    activeTab = SideTab.PROPERTIES
                    layoutEditorOpen = false
                    propertyErrors.clear()
                    rebuildWidgets()
                }.bounds(sideX, contentY + index * 22, sideWidth, 20).build().also {
                    it.active = state.selectedElementId != element.id
                }
            )
        }

        val buttonY = height - 28
        val smallWidth = (sideWidth - 8) / 4
        addRenderableWidget(Button.builder(Component.literal("+")) {
            paletteOpen = true
            rebuildWidgets()
        }.bounds(sideX, buttonY, smallWidth, 20).build())
        addRenderableWidget(Button.builder(Component.literal("−")) { removeSelected() }
            .bounds(sideX + smallWidth + 2, buttonY, smallWidth, 20).build().also {
                it.active = state.selectedElementId != null
            })
        addRenderableWidget(Button.builder(Component.literal("↑")) { moveSelected(-1) }
            .bounds(sideX + (smallWidth + 2) * 2, buttonY, smallWidth, 20).build().also {
                it.active = state.selectedElementId != null
            })
        addRenderableWidget(Button.builder(Component.literal("↓")) { moveSelected(1) }
            .bounds(sideX + (smallWidth + 2) * 3, buttonY, smallWidth, 20).build().also {
                it.active = state.selectedElementId != null
            })
    }

    private fun buildPropertiesTab() {
        val elementId = session.state.selectedElementId ?: return
        val inspected = session.inspectElement(elementId) as? HudElementInspectionResult.Inspected ?: return
        val properties = inspected.model.properties
        if (layoutEditorOpen) {
            val layout = properties.firstOrNull { it.editor is HudPropertyEditorType.LayoutEditor }
                ?: run {
                    layoutEditorOpen = false
                    return
                }
            buildLayoutEditor(elementId, layout)
            return
        }
        sealedEditorPath?.let { path ->
            val property = inspected.model.propertyAt(path)
            if (property?.schema is HudEditablePropertySchema.Sealed) {
                buildSealedEditor(elementId, property)
                return
            }
            sealedEditorPath = null
        }
        val visibleRows = propertyVisibleRows()
        propertyScroll = clampScroll(propertyScroll, properties.size, visibleRows)
        val widgetX = sideX + sideWidth / 2
        val widgetWidth = (sideWidth / 2 - 8).coerceAtLeast(40)
        val firstRowY = previewY + 46
        val factory = HudPropertyWidgetFactory(font)
        properties.drop(propertyScroll).take(visibleRows).forEachIndexed { index, property ->
            val y = firstRowY + index * PROPERTY_ROW_HEIGHT
            val sealed = property.schema as? HudEditablePropertySchema.Sealed
            if (sealed != null) {
                addRenderableWidget(
                    Button.builder(Component.translatable(
                        sealed.variants.single { it.serialName == sealed.selectedVariant }.nameKey
                    )) {
                        sealedEditorPath = property.path
                        sealedEditorScroll = 0
                        rebuildWidgets()
                    }.bounds(widgetX, y, widgetWidth, PROPERTY_WIDGET_HEIGHT).build()
                )
                return@forEachIndexed
            }
            factory.create(
                property = property,
                x = widgetX,
                y = y,
                width = widgetWidth,
                onCommit = { value -> updateProperty(elementId, property, value) },
                onInvalidInput = { message ->
                    propertyErrors[elementId to property.path] = message
                    status = Component.translatable("dynamicrider.hud.editor.property.invalid_input")
                },
                onCancelInput = { clearPropertyError(elementId, property.path) },
                onOpenLayout = {
                    layoutEditorOpen = true
                    sealedEditorPath = null
                    layoutScroll = 0
                    rebuildWidgets()
                },
            ).forEach(::addRenderableWidget)
        }
    }

    private fun buildPreviewStateTab() {
        val presets = session.availablePreviewPresets()
        if (presets.isNotEmpty()) {
            val selected = presets.firstOrNull { it.id == selectedPreviewPresetId } ?: presets.first().also {
                selectedPreviewPresetId = it.id
            }
            previewPresetDropdown = addRenderableWidget(
                DropdownWidget(
                    x = sideX,
                    y = previewY + PREVIEW_PRESET_WIDGET_Y,
                    width = sideWidth - PREVIEW_PRESET_APPLY_WIDTH - 4,
                    height = PROPERTY_WIDGET_HEIGHT,
                    entries = presets.map { preset ->
                        DropdownEntry(preset.id, Component.translatable(preset.displayNameKey))
                    },
                    selected = selected.id,
                    maxVisibleRows = previewPresetVisibleRows(),
                    onSelected = { selectedPreviewPresetId = it },
                )
            )
            addRenderableWidget(
                Button.builder(Component.translatable("dynamicrider.hud.editor.preview_state.apply")) {
                    selectedPreviewPresetId?.let { presetId ->
                        when (session.applyPreviewPreset(presetId)) {
                            is HudEditorActionResult.Applied,
                            HudEditorActionResult.Unchanged,
                            -> {
                                previewStateErrors.clear()
                                status = null
                            }
                            else -> status = Component.translatable("dynamicrider.hud.editor.action_failed")
                        }
                    }
                }.bounds(
                    sideX + sideWidth - PREVIEW_PRESET_APPLY_WIDTH,
                    previewY + PREVIEW_PRESET_WIDGET_Y,
                    PREVIEW_PRESET_APPLY_WIDTH,
                    PROPERTY_WIDGET_HEIGHT,
                ).build()
            )

        }

        val fields = session.previewStateFields()
        val visibleRows = previewStateVisibleRows()
        previewStateScroll = clampScroll(previewStateScroll, fields.size, visibleRows)
        val widgetX = sideX + sideWidth / 2
        val widgetWidth = (sideWidth / 2 - 8).coerceAtLeast(40)
        fields.drop(previewStateScroll).take(visibleRows).forEachIndexed { index, field ->
            val y = previewY + PREVIEW_FIELD_START_Y + index * PROPERTY_ROW_HEIGHT
            when (val editor = field.editor) {
                PreviewStateField.Editor.Toggle -> addRenderableWidget(
                    CycleButton.onOffBuilder(field.value.jsonPrimitive.content.toBoolean())
                        .displayOnlyValue()
                        .create(widgetX, y, widgetWidth, PROPERTY_WIDGET_HEIGHT, Component.translatable(field.nameKey)) { _, value ->
                            updatePreviewState(field, JsonPrimitive(value))
                        }
                )
                is PreviewStateField.Editor.Number -> if (
                    editor.range != null && editor.range.step != null && !editor.nullable
                ) {
                    addPreviewRangeInput(field, editor, widgetX, y, widgetWidth)
                } else {
                    addRenderableWidget(previewNumberInput(field, editor, widgetX, y, widgetWidth))
                }
            }
        }
    }

    private fun addPreviewRangeInput(
        field: PreviewStateField,
        editor: PreviewStateField.Editor.Number,
        x: Int,
        y: Int,
        width: Int,
    ) {
        val range = checkNotNull(editor.range)
        val inputWidth = (width / 3).coerceIn(RANGE_INPUT_MIN_WIDTH, RANGE_INPUT_MAX_WIDTH)
        val sliderWidth = width - inputWidth - RANGE_WIDGET_GAP
        if (sliderWidth < MIN_RANGE_SLIDER_WIDTH) {
            addRenderableWidget(previewNumberInput(field, editor, x, y, width))
            return
        }

        addRenderableWidget(
            HudRangeSlider(
                x = x,
                y = y,
                width = sliderWidth,
                initialValue = field.value.jsonPrimitive.content.toDouble(),
                numberType = editor.numberType,
                range = range,
                onCommit = { value -> updatePreviewState(field, value) },
            )
        )
        addRenderableWidget(
            previewNumberInput(
                field = field,
                editor = editor,
                x = x + sliderWidth + RANGE_WIDGET_GAP,
                y = y,
                width = inputWidth,
            )
        )
    }

    private fun previewNumberInput(
        field: PreviewStateField,
        editor: PreviewStateField.Editor.Number,
        x: Int,
        y: Int,
        width: Int,
    ): HudCommitEditBox = HudCommitEditBox(
        font = font,
        x = x,
        y = y,
        width = width,
        height = PROPERTY_WIDGET_HEIGHT,
        message = Component.translatable(field.nameKey),
        initialValue = if (field.value === JsonNull) "null" else field.value.jsonPrimitive.content,
        onCommit = { input ->
            runCatching {
                if (editor.nullable && input.equals("null", ignoreCase = true)) {
                    JsonNull
                } else {
                    when (editor.numberType) {
                        HudNumberType.BYTE -> JsonPrimitive(input.toByte())
                        HudNumberType.SHORT -> JsonPrimitive(input.toShort())
                        HudNumberType.INT -> JsonPrimitive(input.toInt())
                        HudNumberType.LONG -> JsonPrimitive(input.toLong())
                        HudNumberType.FLOAT -> JsonPrimitive(input.toFloat())
                        HudNumberType.DOUBLE -> JsonPrimitive(input.toDouble())
                    }
                }
            }.fold(
                onSuccess = { updatePreviewState(field, it) },
                onFailure = {
                    previewStateErrors[field.id] = it.message ?: "Invalid value"
                    status = Component.translatable("dynamicrider.hud.editor.property.invalid_input")
                    false
                },
            )
        },
        onCancel = {
            previewStateErrors.remove(field.id)
            if (previewStateErrors.isEmpty()) status = null
        },
    ).also { it.setMaxLength(128) }

    private fun updatePreviewState(field: PreviewStateField, value: JsonElement): Boolean =
        when (session.updatePreviewState(field.id, value)) {
            is HudEditorActionResult.Applied,
            HudEditorActionResult.Unchanged,
            -> {
                previewStateErrors.remove(field.id)
                if (previewStateErrors.isEmpty()) status = null
                true
            }
            else -> {
                previewStateErrors[field.id] = "Invalid value"
                status = Component.translatable("dynamicrider.hud.editor.property.rejected")
                false
            }
        }

    private fun buildLayoutEditor(elementId: String, layout: HudEditableProperty) {
        val fields = HudLayoutEditorModel.fields(layout)
        val visibleRows = layoutVisibleRows()
        layoutScroll = clampScroll(layoutScroll, fields.size, visibleRows)
        addRenderableWidget(
            Button.builder(Component.translatable("gui.back")) {
                layoutEditorOpen = false
                rebuildWidgets()
            }.bounds(sideX, previewY + 26, 52, 20).build()
        )
        val widgetX = sideX + sideWidth / 2
        val widgetWidth = (sideWidth / 2 - 8).coerceAtLeast(40)
        val firstRowY = previewY + 52
        val factory = HudPropertyWidgetFactory(font)
        fields.drop(layoutScroll).take(visibleRows).forEachIndexed { index, field ->
            val y = firstRowY + index * PROPERTY_ROW_HEIGHT
            factory.create(
                property = field,
                x = widgetX,
                y = y,
                width = widgetWidth,
                onCommit = { value ->
                    val replacement = HudLayoutEditorModel.replace(layout, field, value)
                    updateProperty(elementId, layout, replacement, field.path)
                },
                onInvalidInput = { message ->
                    propertyErrors[elementId to field.path] = message
                    status = Component.translatable("dynamicrider.hud.editor.property.invalid_input")
                },
                onCancelInput = { clearPropertyError(elementId, field.path) },
            ).forEach(::addRenderableWidget)
        }
    }

    private fun buildSealedEditor(elementId: String, property: HudEditableProperty) {
        val schema = property.schema as? HudEditablePropertySchema.Sealed ?: return
        addRenderableWidget(
            Button.builder(Component.translatable("gui.back")) {
                sealedEditorPath = null
                rebuildWidgets()
            }.bounds(sideX, previewY + 26, 52, PROPERTY_WIDGET_HEIGHT).build()
        )
        sealedVariantDropdown = addRenderableWidget(
            DropdownWidget(
                x = sideX + 58,
                y = previewY + 26,
                width = (sideWidth - 58).coerceAtLeast(40),
                height = PROPERTY_WIDGET_HEIGHT,
                entries = schema.variants.map { DropdownEntry(it.serialName, Component.translatable(it.nameKey)) },
                selected = schema.selectedVariant,
                maxVisibleRows = sealedVisibleRows().coerceAtLeast(1),
                onSelected = { variant ->
                    when (val result = session.changePropertyVariant(elementId, property.path, variant)) {
                        is HudEditorActionResult.Applied,
                        HudEditorActionResult.Unchanged,
                        -> {
                            propertyErrors.remove(elementId to property.path)
                            status = null
                        }
                        is HudEditorActionResult.PropertyRejected -> {
                            propertyErrors[elementId to property.path] = result.failure.message
                            status = Component.translatable("dynamicrider.hud.editor.property.rejected")
                        }
                        else -> status = Component.translatable("dynamicrider.hud.editor.action_failed")
                    }
                    rebuildWidgets()
                },
            )
        )

        val visibleRows = sealedVisibleRows()
        sealedEditorScroll = clampScroll(sealedEditorScroll, schema.properties.size, visibleRows)
        val widgetX = sideX + sideWidth / 2
        val widgetWidth = (sideWidth / 2 - 8).coerceAtLeast(40)
        val firstRowY = previewY + 52
        val factory = HudPropertyWidgetFactory(font)
        schema.properties.drop(sealedEditorScroll).take(visibleRows).forEachIndexed { index, child ->
            val y = firstRowY + index * PROPERTY_ROW_HEIGHT
            val nested = child.schema as? HudEditablePropertySchema.Sealed
            if (nested != null) {
                addRenderableWidget(
                    Button.builder(Component.translatable(
                        nested.variants.single { it.serialName == nested.selectedVariant }.nameKey
                    )) {
                        sealedEditorPath = child.path
                        sealedEditorScroll = 0
                        rebuildWidgets()
                    }.bounds(widgetX, y, widgetWidth, PROPERTY_WIDGET_HEIGHT).build()
                )
            } else {
                factory.create(
                    property = child,
                    x = widgetX,
                    y = y,
                    width = widgetWidth,
                    onCommit = { value -> updateProperty(elementId, child, value) },
                    onInvalidInput = { message ->
                        propertyErrors[elementId to child.path] = message
                        status = Component.translatable("dynamicrider.hud.editor.property.invalid_input")
                    },
                    onCancelInput = { clearPropertyError(elementId, child.path) },
                ).forEach(::addRenderableWidget)
            }
        }
    }

    private fun renderEditorChrome(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        guiGraphics.fill(0, 0, width, 32, 0xD0101010.toInt())
        guiGraphics.fill(previewX, previewY, previewX + previewWidth, previewY + previewHeight, 0xC0181818.toInt())
        guiGraphics.renderOutline(previewX, previewY, previewWidth, previewHeight, 0xFF606060.toInt())
        guiGraphics.fill(sideX - 4, previewY, width, height, 0xE0202020.toInt())
        val dividerColor = if (resizingSidePanel || isOverDivider(mouseX.toDouble(), mouseY.toDouble())) {
            0xFFAAAAAA.toInt()
        } else {
            0xFF555555.toInt()
        }
        guiGraphics.fill(sideX - DIVIDER_HALF_WIDTH, previewY, sideX - DIVIDER_HALF_WIDTH + 2, height, dividerColor)

        val state = session.state
        val marker = if (state.dirty) " *" else ""
        val sceneLabel = "${state.mode.name} / ${state.kartStateType.id.uppercase()}$marker"
        guiGraphics.drawCenteredString(font, sceneLabel, width / 2 - 48, 12, 0xFFFFFF)
        status?.let { guiGraphics.drawString(font, it, previewX + 4, height - 18, 0xFFFF7777.toInt()) }
    }

    private fun renderPreview(guiGraphics: GuiGraphics) {
        val transform = previewTransform
        val scissorLeft = floor(transform.contentX).toInt()
        val scissorTop = floor(transform.contentY).toInt()
        val scissorRight = ceil(transform.contentX + transform.contentWidth).toInt()
        val scissorBottom = ceil(transform.contentY + transform.contentHeight).toInt()
        guiGraphics.enableScissor(scissorLeft, scissorTop, scissorRight, scissorBottom)
        guiGraphics.pose().pushPose()
        guiGraphics.pose().translate(transform.contentX, transform.contentY, 0f)
        guiGraphics.pose().scale(transform.scale, transform.scale, 1f)
        session.previewScene.draw(guiGraphics, Minecraft.getInstance().deltaTracker)
        guiGraphics.pose().popPose()
        if (!previewMode) renderSelectionOverlay(guiGraphics, transform)
        guiGraphics.disableScissor()
    }

    private fun renderPreviewModeHint(guiGraphics: GuiGraphics) {
        guiGraphics.pose().pushPose()
        guiGraphics.pose().scale(PREVIEW_HINT_SCALE, PREVIEW_HINT_SCALE, 1f)
        guiGraphics.drawCenteredString(
            font,
            Component.translatable("dynamicrider.hud.editor.preview_mode.hint"),
            (width / PREVIEW_HINT_SCALE / 2f).roundToInt(),
            (height / PREVIEW_HINT_SCALE / 2f - font.lineHeight / 2f).roundToInt(),
            PREVIEW_HINT_COLOR,
        )
        guiGraphics.pose().popPose()
    }

    private fun renderSideContent(guiGraphics: GuiGraphics) {
        val contentY = previewY + 28
        when (activeTab) {
            SideTab.ELEMENTS -> if (session.state.elements.isEmpty() && !paletteOpen) {
                guiGraphics.drawWordWrap(
                    font,
                    Component.translatable("dynamicrider.hud.editor.elements.empty"),
                    sideX + 6,
                    contentY,
                    sideWidth - 12,
                    0xFFBBBBBB.toInt(),
                )
            }
            SideTab.PROPERTIES -> renderProperties(guiGraphics, contentY)
            SideTab.PREVIEW_STATE -> renderPreviewState(guiGraphics)
        }
        currentScrollMetrics()?.let { renderScrollBar(guiGraphics, it) }
    }

    private fun renderPreviewState(guiGraphics: GuiGraphics) {
        guiGraphics.drawString(
            font,
            Component.translatable("dynamicrider.hud.editor.preview_state.preset"),
            sideX + 6,
            previewY + 30,
            0xFFDDDDDD.toInt(),
        )
        val fields = session.previewStateFields()
        val visibleRows = previewStateVisibleRows()
        previewStateScroll = clampScroll(previewStateScroll, fields.size, visibleRows)
        fields.drop(previewStateScroll).take(visibleRows).forEachIndexed { index, field ->
            val y = previewY + PREVIEW_FIELD_START_Y + index * PROPERTY_ROW_HEIGHT
            val color = if (previewStateErrors.containsKey(field.id)) 0xFFFF6666.toInt() else 0xFFDDDDDD.toInt()
            guiGraphics.drawString(font, Component.translatable(field.nameKey), sideX + 6, centeredLabelY(y), color)
            previewStateErrors[field.id]?.let { message ->
                guiGraphics.drawString(font, message, sideX + 6, y + 21, 0xFFFF6666.toInt())
            }
        }
    }

    private fun renderProperties(guiGraphics: GuiGraphics, contentY: Int) {
        val selectedId = session.state.selectedElementId
        if (selectedId == null) {
            guiGraphics.drawWordWrap(
                font,
                Component.translatable("dynamicrider.hud.editor.properties.empty"),
                sideX + 6,
                contentY,
                sideWidth - 12,
                0xFFBBBBBB.toInt(),
            )
            return
        }
        val inspected = session.inspectElement(selectedId)
        if (inspected !is HudElementInspectionResult.Inspected) {
            guiGraphics.drawString(font, Component.translatable("dynamicrider.hud.editor.inspect_failed"), sideX + 6, contentY, 0xFFFF7777.toInt())
            return
        }
        if (layoutEditorOpen) {
            val layout = inspected.model.properties.firstOrNull { it.editor is HudPropertyEditorType.LayoutEditor }
            if (layout != null) {
                renderLayoutEditor(guiGraphics, selectedId, layout)
                return
            }
        }
        sealedEditorPath?.let { path ->
            val property = inspected.model.propertyAt(path)
            if (property?.schema is HudEditablePropertySchema.Sealed) {
                renderSealedEditor(guiGraphics, selectedId, property)
                return
            }
        }
        guiGraphics.drawString(font, Component.translatable(inspected.model.nameKey), sideX + 6, contentY, 0xFFFFFFFF.toInt())
        val maxRows = propertyVisibleRows()
        propertyScroll = clampScroll(propertyScroll, inspected.model.properties.size, maxRows)
        inspected.model.properties.drop(propertyScroll).take(maxRows).forEachIndexed { index, property ->
            val y = contentY + 18 + index * PROPERTY_ROW_HEIGHT
            val color = when {
                propertyErrors.containsKey(selectedId to property.path) -> 0xFFFF6666.toInt()
                property.supported -> 0xFFDDDDDD.toInt()
                else -> 0xFF777777.toInt()
            }
            guiGraphics.drawString(font, Component.translatable(property.nameKey), sideX + 6, centeredLabelY(y), color)
            propertyErrors[selectedId to property.path]?.let { message ->
                guiGraphics.drawString(font, message, sideX + 6, y + 21, 0xFFFF6666.toInt())
            }
        }
    }

    private fun renderLayoutEditor(
        guiGraphics: GuiGraphics,
        elementId: String,
        layout: HudEditableProperty,
    ) {
        guiGraphics.drawString(
            font,
            Component.translatable("dynamicrider.hud.editor.layout.title"),
            sideX + 58,
            previewY + 32,
            0xFFFFFFFF.toInt(),
        )
        val fields = HudLayoutEditorModel.fields(layout)
        val visibleRows = layoutVisibleRows()
        layoutScroll = clampScroll(layoutScroll, fields.size, visibleRows)
        fields.drop(layoutScroll).take(visibleRows).forEachIndexed { index, field ->
            val y = previewY + 52 + index * PROPERTY_ROW_HEIGHT
            guiGraphics.drawString(
                font,
                Component.translatable(field.nameKey),
                sideX + 6,
                centeredLabelY(y),
                if (propertyErrors.containsKey(elementId to field.path)) {
                    0xFFFF6666.toInt()
                } else {
                    0xFFDDDDDD.toInt()
                },
            )
            propertyErrors[elementId to field.path]?.let { message ->
                guiGraphics.drawString(font, message, sideX + 6, y + 21, 0xFFFF6666.toInt())
            }
        }
    }

    private fun renderSealedEditor(
        guiGraphics: GuiGraphics,
        elementId: String,
        property: HudEditableProperty,
    ) {
        val schema = property.schema as? HudEditablePropertySchema.Sealed ?: return
        val visibleRows = sealedVisibleRows()
        sealedEditorScroll = clampScroll(sealedEditorScroll, schema.properties.size, visibleRows)
        schema.properties.drop(sealedEditorScroll).take(visibleRows).forEachIndexed { index, child ->
            val y = previewY + 52 + index * PROPERTY_ROW_HEIGHT
            val color = if (propertyErrors.containsKey(elementId to child.path)) {
                0xFFFF6666.toInt()
            } else {
                0xFFDDDDDD.toInt()
            }
            guiGraphics.drawString(font, Component.translatable(child.nameKey), sideX + 6, centeredLabelY(y), color)
            propertyErrors[elementId to child.path]?.let { message ->
                guiGraphics.drawString(font, message, sideX + 6, y + 21, 0xFFFF6666.toInt())
            }
        }
    }

    private fun buildScenePicker(picker: EditorModal.ScenePicker) {
        disableEditorWidgetsForModal()
        val left = modalLeft()
        val top = modalTop(scenePickerHeight())
        val contentWidth = MODAL_WIDTH - 24
        val modeWidth = (contentWidth - 4) / 2
        HudSceneMode.entries.forEachIndexed { index, mode ->
            addRenderableWidget(
                Button.builder(Component.literal(mode.name)) {
                    picker.mode = mode
                    rebuildWidgets()
                }.bounds(left + 12 + index * (modeWidth + 4), top + 34, modeWidth, 20)
                    .build()
                    .also { it.active = picker.mode != mode }
            )
        }

        val visibleRows = scenePickerVisibleRows()
        picker.scroll = clampScroll(picker.scroll, KartStateTypes.entries.size, visibleRows)
        KartStateTypes.entries.drop(picker.scroll).take(visibleRows).forEachIndexed { index, stateType ->
            addRenderableWidget(
                Button.builder(Component.literal(stateType.id.uppercase())) {
                    picker.stateType = stateType
                    rebuildWidgets()
                }.bounds(left + 12, top + 64 + index * ROW_HEIGHT, contentWidth, 20)
                    .build()
                    .also { it.active = picker.stateType != stateType }
            )
        }

        val buttonY = top + scenePickerHeight() - 30
        addRenderableWidget(
            Button.builder(Component.translatable("dynamicrider.hud.editor.scene.open")) {
                requestDeparture(DepartureAction.SwitchScene(picker.mode, picker.stateType))
            }.bounds(left + 12, buttonY, (contentWidth - 4) / 2, 20).build()
        )
        addRenderableWidget(
            Button.builder(Component.translatable("gui.cancel")) {
                modal = null
                rebuildWidgets()
            }.bounds(left + 16 + (contentWidth - 4) / 2, buttonY, (contentWidth - 4) / 2, 20).build()
        )
    }

    private fun buildDepartureConfirmation(action: DepartureAction) {
        disableEditorWidgetsForModal()
        val left = modalLeft()
        val top = modalTop(CONFIRM_MODAL_HEIGHT)
        val contentWidth = MODAL_WIDTH - 24
        val buttonWidth = (contentWidth - 8) / 3
        val buttonY = top + CONFIRM_MODAL_HEIGHT - 30
        addRenderableWidget(
            Button.builder(Component.translatable("dynamicrider.hud.editor.unsaved.save")) {
                if (save()) performDeparture(action) else {
                    modal = null
                    rebuildWidgets()
                }
            }.bounds(left + 12, buttonY, buttonWidth, 20).build()
        )
        addRenderableWidget(
            Button.builder(Component.translatable("dynamicrider.hud.editor.unsaved.discard")) {
                performDeparture(action)
            }.bounds(left + 16 + buttonWidth, buttonY, buttonWidth, 20).build()
        )
        addRenderableWidget(
            Button.builder(Component.translatable("gui.cancel")) {
                modal = null
                rebuildWidgets()
            }.bounds(left + 20 + buttonWidth * 2, buttonY, buttonWidth, 20).build()
        )
    }

    private fun buildRestoreConfirmation() {
        disableEditorWidgetsForModal()
        val left = modalLeft()
        val top = modalTop(CONFIRM_MODAL_HEIGHT)
        val contentWidth = MODAL_WIDTH - 24
        val buttonWidth = (contentWidth - 4) / 2
        val buttonY = top + CONFIRM_MODAL_HEIGHT - 30
        addRenderableWidget(
            Button.builder(Component.translatable("dynamicrider.hud.editor.restore.confirm")) {
                performRestore()
            }.bounds(left + 12, buttonY, buttonWidth, 20).build()
        )
        addRenderableWidget(
            Button.builder(Component.translatable("gui.cancel")) {
                modal = null
                rebuildWidgets()
            }.bounds(left + 16 + buttonWidth, buttonY, buttonWidth, 20).build()
        )
    }

    private fun renderModal(guiGraphics: GuiGraphics) {
        val currentModal = modal ?: return
        guiGraphics.fill(0, 0, width, height, 0xA0000000.toInt())
        val modalHeight = when (currentModal) {
            is EditorModal.ScenePicker -> scenePickerHeight()
            is EditorModal.ConfirmDeparture -> CONFIRM_MODAL_HEIGHT
            EditorModal.ConfirmRestore -> CONFIRM_MODAL_HEIGHT
        }
        val left = modalLeft()
        val top = modalTop(modalHeight)
        guiGraphics.fill(left, top, left + MODAL_WIDTH, top + modalHeight, 0xFF202020.toInt())
        guiGraphics.renderOutline(left, top, MODAL_WIDTH, modalHeight, 0xFF808080.toInt())
        when (currentModal) {
            is EditorModal.ScenePicker -> {
                guiGraphics.drawCenteredString(
                    font,
                    Component.translatable("dynamicrider.hud.editor.scene.title"),
                    width / 2,
                    top + 12,
                    0xFFFFFFFF.toInt(),
                )
                guiGraphics.drawString(
                    font,
                    Component.translatable("dynamicrider.hud.editor.state_type"),
                    left + 12,
                    top + 55,
                    0xFFBBBBBB.toInt(),
                )
            }
            is EditorModal.ConfirmDeparture -> {
                guiGraphics.drawCenteredString(
                    font,
                    Component.translatable("dynamicrider.hud.editor.unsaved.title"),
                    width / 2,
                    top + 14,
                    0xFFFFFFFF.toInt(),
                )
                guiGraphics.drawWordWrap(
                    font,
                    Component.translatable("dynamicrider.hud.editor.unsaved.message"),
                    left + 16,
                    top + 36,
                    MODAL_WIDTH - 32,
                    0xFFDDDDDD.toInt(),
                )
            }
            EditorModal.ConfirmRestore -> {
                guiGraphics.drawCenteredString(
                    font,
                    Component.translatable("dynamicrider.hud.editor.restore.warning.title"),
                    width / 2,
                    top + 14,
                    0xFFFFFFFF.toInt(),
                )
                guiGraphics.drawWordWrap(
                    font,
                    Component.translatable("dynamicrider.hud.editor.restore.warning.message"),
                    left + 16,
                    top + 36,
                    MODAL_WIDTH - 32,
                    0xFFDDDDDD.toInt(),
                )
            }
        }
    }

    private fun openScenePicker() {
        val state = session.state
        val visibleRows = scenePickerVisibleRows()
        val selectedIndex = KartStateTypes.entries.indexOf(state.kartStateType).coerceAtLeast(0)
        modal = EditorModal.ScenePicker(
            state.mode,
            state.kartStateType,
            selectedIndex.coerceAtMost((KartStateTypes.entries.size - visibleRows).coerceAtLeast(0)),
        )
        rebuildWidgets()
    }

    private fun openRestoreConfirmation() {
        modal = EditorModal.ConfirmRestore
        rebuildWidgets()
    }

    private fun requestDeparture(action: DepartureAction) {
        if (closing) return
        if (action is DepartureAction.SwitchScene &&
            action.mode == session.mode && action.stateType == session.previewContext.kartStateType
        ) {
            modal = null
            rebuildWidgets()
            return
        }
        if (session.state.dirty) {
            modal = EditorModal.ConfirmDeparture(action)
            rebuildWidgets()
        } else {
            performDeparture(action)
        }
    }

    private fun performDeparture(action: DepartureAction) {
        when (action) {
            DepartureAction.Exit -> {
                closing = true
                Minecraft.getInstance().setScreen(parentScreen)
            }
            is DepartureAction.SwitchScene -> switchScene(action.mode, action.stateType)
        }
    }

    private fun switchScene(mode: HudSceneMode, stateType: KartStateType<out KartState>) {
        val viewport = HudEditorPreviewViewport()
        when (val result = sessionFactory.open(mode, stateType, viewport)) {
            is HudEditorSessionOpenResult.Opened -> {
                HudEditorSceneSelection.remember(mode, stateType)
                closing = true
                Minecraft.getInstance().setScreen(
                    HudEditorScreen(parentScreen, sessionFactory, result.session, viewport)
                )
            }
            is HudEditorSessionOpenResult.ResolveFailed -> {
                status = Component.translatable("dynamicrider.hud.editor.open_failed", result.errors.size)
                modal = null
                rebuildWidgets()
            }
            is HudEditorSessionOpenResult.PreviewCreationFailed -> {
                status = Component.translatable("dynamicrider.hud.editor.preview_failed")
                modal = null
                rebuildWidgets()
            }
        }
    }

    private fun toolbarButton(x: Int, key: String, action: () -> Unit): Button =
        Button.builder(Component.translatable(key)) { action() }.bounds(x, 6, 60, 20).build()

    private fun disableEditorWidgetsForModal() {
        editorWidgets.forEach { it.active = false }
    }

    private fun removeSelected() {
        session.state.selectedElementId?.let(session::removeElement)
    }

    private fun moveSelected(offset: Int) {
        val state = session.state
        val id = state.selectedElementId ?: return
        val index = state.elements.indexOfFirst { it.id == id }
        if (index >= 0) session.moveElement(id, index + offset)
    }

    private fun save(): Boolean {
        when (session.save()) {
            is HudEditorPersistenceResult.Saved -> {
                status = Component.translatable("dynamicrider.hud.editor.saved")
                return true
            }
            is HudEditorPersistenceResult.IoFailed -> {
                status = Component.translatable("dynamicrider.hud.editor.save_failed")
            }
            else -> {
                status = Component.translatable("dynamicrider.hud.editor.action_failed")
            }
        }
        return false
    }

    private fun performRestore() {
        val result = session.deleteCustom(discardUnsavedChanges = true)
        status = when (result) {
            is HudEditorPersistenceResult.Restored -> Component.translatable("dynamicrider.hud.editor.restored")
            is HudEditorPersistenceResult.IoFailed -> Component.translatable("dynamicrider.hud.editor.restore_failed")
            is HudEditorPersistenceResult.ResolveFailed -> Component.translatable("dynamicrider.hud.editor.restore_failed")
            else -> Component.translatable("dynamicrider.hud.editor.action_failed")
        }
        modal = null
        rebuildWidgets()
    }

    private fun updateProperty(
        elementId: String,
        property: HudEditableProperty,
        value: kotlinx.serialization.json.JsonElement,
        errorPath: HudPropertyPath = property.path,
    ): Boolean {
        val key = elementId to errorPath
        return when (val result = session.updateProperty(elementId, property.path, value)) {
            is HudEditorActionResult.Applied -> {
                propertyErrors.remove(key)
                status = null
                true
            }
            HudEditorActionResult.Unchanged -> {
                propertyErrors.remove(key)
                status = null
                rebuildWidgets()
                true
            }
            is HudEditorActionResult.PropertyRejected -> {
                propertyErrors[key] = result.failure.message
                status = Component.translatable("dynamicrider.hud.editor.property.rejected")
                false
            }
            else -> {
                status = Component.translatable("dynamicrider.hud.editor.action_failed")
                false
            }
        }
    }

    private fun clearPropertyError(elementId: String, path: HudPropertyPath) {
        propertyErrors.remove(elementId to path)
        if (propertyErrors.isEmpty()) status = null
    }

    private fun dragSelectedElement(mouseX: Double, mouseY: Double, drag: ElementDrag) {
        val guide = previewGuides().firstOrNull { it.elementId == drag.elementId } ?: return
        val inspected = session.inspectElement(drag.elementId) as? HudElementInspectionResult.Inspected ?: return
        val layout = inspected.model.properties.firstOrNull { it.editor is HudPropertyEditorType.LayoutEditor } ?: return
        val pointer = previewTransform.screenToLogical(mouseX, mouseY)
        val anchorX = pointer.x - drag.grabOffsetX
        val anchorY = pointer.y - drag.grabOffsetY
        val (x, y) = HudLayoutEngine.offsetForElementAnchor(
            guide.screenAnchorX,
            guide.screenAnchorY,
            anchorX,
            anchorY,
        )
        val replacement = HudLayoutEditorModel.replacePosition(layout, x, y)
        when (session.updateProperty(drag.elementId, layout.path, replacement, drag.commandStarted)) {
            is HudEditorActionResult.Applied -> {
                drag.commandStarted = true
                status = null
            }
            is HudEditorActionResult.PropertyRejected -> status =
                Component.translatable("dynamicrider.hud.editor.property.rejected")
            else -> Unit
        }
    }

    private fun renderSelectionOverlay(guiGraphics: GuiGraphics, transform: HudPreviewTransform) {
        val selectedId = session.state.selectedElementId ?: return
        val guide = previewGuides().firstOrNull { it.elementId == selectedId } ?: return
        val topLeft = transform.logicalToScreen(guide.bounds.left, guide.bounds.top)
        val bottomRight = transform.logicalToScreen(guide.bounds.right, guide.bounds.bottom)
        val left = topLeft.x.roundToInt()
        val top = topLeft.y.roundToInt()
        val right = bottomRight.x.roundToInt()
        val bottom = bottomRight.y.roundToInt()
        guiGraphics.renderOutline(
            left,
            top,
            (right - left).coerceAtLeast(1),
            (bottom - top).coerceAtLeast(1),
            SELECTION_COLOR,
        )

        val screenAnchor = transform.logicalToScreen(guide.screenAnchorX, guide.screenAnchorY)
        val elementAnchor = transform.logicalToScreen(guide.elementAnchorX, guide.elementAnchorY)
        val screenX = screenAnchor.x.roundToInt()
        val screenY = screenAnchor.y.roundToInt()
        val elementX = elementAnchor.x.roundToInt()
        val elementY = elementAnchor.y.roundToInt()
        drawAnchorCross(guiGraphics, screenX, screenY, SCREEN_ANCHOR_COLOR, 4)
        drawAnchorLine(guiGraphics, screenX.toFloat(), screenY.toFloat(), elementX.toFloat(), elementY.toFloat())
        drawAnchorCross(guiGraphics, elementX, elementY, ELEMENT_ANCHOR_COLOR, 3)
        val (handleX, handleY) = guide.scaleHandle()
        val handle = transform.logicalToScreen(handleX, handleY)
        guiGraphics.fill(
            handle.x.roundToInt() - SCALE_HANDLE_RADIUS,
            handle.y.roundToInt() - SCALE_HANDLE_RADIUS,
            handle.x.roundToInt() + SCALE_HANDLE_RADIUS + 1,
            handle.y.roundToInt() + SCALE_HANDLE_RADIUS + 1,
            SCALE_HANDLE_COLOR,
        )
    }

    private fun drawAnchorCross(guiGraphics: GuiGraphics, x: Int, y: Int, color: Int, radius: Int) {
        guiGraphics.fill(x - radius, y, x + radius + 1, y + 1, color)
        guiGraphics.fill(x, y - radius, x + 1, y + radius + 1, color)
    }

    private fun drawAnchorLine(guiGraphics: GuiGraphics, startX: Float, startY: Float, endX: Float, endY: Float) {
        val dx = endX - startX
        val dy = endY - startY
        val length = sqrt(dx * dx + dy * dy)
        if (length == 0f) return
        val perpendicularX = -dy / length * 0.5f
        val perpendicularY = dx / length * 0.5f
        guiGraphics.batch(
            VertexFormat.Mode.QUADS,
            DefaultVertexFormat.POSITION_COLOR,
            DynRiderRenderTypes.ARC_CORE,
        ) { poseMatrix ->
            addVertex(poseMatrix, startX + perpendicularX, startY + perpendicularY, 0f).setColor(OFFSET_COLOR)
            addVertex(poseMatrix, endX + perpendicularX, endY + perpendicularY, 0f).setColor(OFFSET_COLOR)
            addVertex(poseMatrix, endX - perpendicularX, endY - perpendicularY, 0f).setColor(OFFSET_COLOR)
            addVertex(poseMatrix, startX - perpendicularX, startY - perpendicularY, 0f).setColor(OFFSET_COLOR)
        }
    }

    private fun beginScaleDrag(guide: HudPreviewElementGuide) {
        val element = session.previewScene.entries.firstOrNull { it.id == guide.elementId }?.element ?: return
        val (handleX, handleY) = guide.scaleHandle()
        val vectorX = handleX - guide.elementAnchorX
        val vectorY = handleY - guide.elementAnchorY
        val vectorLengthSquared = vectorX * vectorX + vectorY * vectorY
        if (vectorLengthSquared == 0f) return
        scaleDrag = ScaleDrag(
            elementId = guide.elementId,
            anchorX = guide.elementAnchorX,
            anchorY = guide.elementAnchorY,
            handleVectorX = vectorX,
            handleVectorY = vectorY,
            handleLengthSquared = vectorLengthSquared,
            initialScale = (abs(element.scale.x) + abs(element.scale.y)) / 2f,
        )
        elementDrag = null
        isDragging = true
    }

    private fun scaleSelectedElement(mouseX: Double, mouseY: Double, drag: ScaleDrag) {
        val inspected = session.inspectElement(drag.elementId) as? HudElementInspectionResult.Inspected ?: return
        val layout = inspected.model.properties.firstOrNull { it.editor is HudPropertyEditorType.LayoutEditor } ?: return
        val pointer = previewTransform.screenToLogical(mouseX, mouseY)
        val pointerX = pointer.x - drag.anchorX
        val pointerY = pointer.y - drag.anchorY
        val ratio = (pointerX * drag.handleVectorX + pointerY * drag.handleVectorY) / drag.handleLengthSquared
        val scale = (drag.initialScale * ratio)
            .coerceIn(MIN_UNIFORM_SCALE, MAX_UNIFORM_SCALE)
            .let { (it * 100f).roundToInt() / 100f }
        val replacement = HudLayoutEditorModel.replaceUniformScale(layout, scale)
        when (session.updateProperty(drag.elementId, layout.path, replacement, drag.commandStarted)) {
            is HudEditorActionResult.Applied -> {
                drag.commandStarted = true
                status = null
            }
            is HudEditorActionResult.PropertyRejected -> status =
                Component.translatable("dynamicrider.hud.editor.property.rejected")
            else -> Unit
        }
    }

    private fun isOverScaleHandle(guide: HudPreviewElementGuide, x: Double, y: Double): Boolean {
        val (handleX, handleY) = guide.scaleHandle()
        val handle = previewTransform.logicalToScreen(handleX, handleY)
        return abs(x - handle.x) <= SCALE_HANDLE_HIT_RADIUS && abs(y - handle.y) <= SCALE_HANDLE_HIT_RADIUS
    }

    private fun previewGuides(): List<HudPreviewElementGuide> =
        HudPreviewElementGuideCalculator.calculate(session.previewScene)

    private fun isInsidePreview(mouseX: Double, mouseY: Double): Boolean =
        previewTransform.containsScreenPoint(mouseX, mouseY)

    private fun enterPreviewMode() {
        if (modal != null) return
        previewMode = true
        elementDrag = null
        scaleDrag = null
        resizingSidePanel = false
        isDragging = false
        clearFocus()
        rebuildWidgets()
    }

    private fun exitPreviewMode() {
        previewMode = false
        clearFocus()
        rebuildWidgets()
    }

    private fun selectedProperties() = session.state.selectedElementId
        ?.let(session::inspectElement)
        ?.let { (it as? HudElementInspectionResult.Inspected)?.model?.properties }

    private fun selectedLayoutFields(): List<HudEditableProperty>? = selectedProperties()
        ?.firstOrNull { it.editor is HudPropertyEditorType.LayoutEditor }
        ?.let(HudLayoutEditorModel::fields)

    private fun selectedSealedProperties(): List<HudEditableProperty>? {
        val path = sealedEditorPath ?: return null
        val selectedId = session.state.selectedElementId ?: return null
        val model = (session.inspectElement(selectedId) as? HudElementInspectionResult.Inspected)?.model ?: return null
        return (model.propertyAt(path)?.schema as? HudEditablePropertySchema.Sealed)?.properties
    }

    private fun elementVisibleRows(): Int = ((height - (previewY + 26) - 58) / ROW_HEIGHT).coerceAtLeast(0)

    private fun paletteVisibleRows(): Int = ((height - (previewY + 26) - 34) / ROW_HEIGHT).coerceAtLeast(0)

    private fun propertyVisibleRows(): Int = ((height - (previewY + 28) - 24) / PROPERTY_ROW_HEIGHT).coerceAtLeast(0)

    private fun layoutVisibleRows(): Int = ((height - (previewY + 52) - 8) / PROPERTY_ROW_HEIGHT).coerceAtLeast(0)

    private fun sealedVisibleRows(): Int = ((height - (previewY + 52) - 8) / PROPERTY_ROW_HEIGHT).coerceAtLeast(0)

    private fun previewStateVisibleRows(): Int =
        ((height - (previewY + PREVIEW_FIELD_START_Y) - 8) / PROPERTY_ROW_HEIGHT).coerceAtLeast(0)

    private fun previewPresetVisibleRows(): Int =
        ((height - (previewY + PREVIEW_FIELD_START_Y) - 8) / ROW_HEIGHT).coerceAtLeast(1)

    private fun centeredLabelY(rowY: Int): Int = rowY + (PROPERTY_WIDGET_HEIGHT - font.lineHeight) / 2

    private fun scenePickerHeight(): Int = min(SCENE_PICKER_MAX_HEIGHT, height - 32).coerceAtLeast(160)

    private fun scenePickerVisibleRows(): Int = ((scenePickerHeight() - 104) / ROW_HEIGHT).coerceAtLeast(1)

    private fun modalLeft(): Int = (width - MODAL_WIDTH) / 2

    private fun modalTop(modalHeight: Int): Int = (height - modalHeight) / 2

    private fun clampScroll(offset: Int, itemCount: Int, visibleRows: Int): Int =
        offset.coerceIn(0, (itemCount - visibleRows).coerceAtLeast(0))

    private data class ScrollMetrics(
        val offset: Int,
        val itemCount: Int,
        val visibleRows: Int,
    )

    private data class ElementDrag(
        val elementId: String,
        val grabOffsetX: Float,
        val grabOffsetY: Float,
        var commandStarted: Boolean = false,
    )

    private data class ScaleDrag(
        val elementId: String,
        val anchorX: Float,
        val anchorY: Float,
        val handleVectorX: Float,
        val handleVectorY: Float,
        val handleLengthSquared: Float,
        val initialScale: Float,
        var commandStarted: Boolean = false,
    )

    private sealed interface EditorModal {
        data class ScenePicker(
            var mode: HudSceneMode,
            var stateType: KartStateType<out KartState>,
            var scroll: Int = 0,
        ) : EditorModal

        data class ConfirmDeparture(val action: DepartureAction) : EditorModal

        data object ConfirmRestore : EditorModal
    }

    private sealed interface DepartureAction {
        data object Exit : DepartureAction

        data class SwitchScene(
            val mode: HudSceneMode,
            val stateType: KartStateType<out KartState>,
        ) : DepartureAction
    }

    private fun currentScrollMetrics(): ScrollMetrics? = when (activeTab) {
        SideTab.ELEMENTS -> if (paletteOpen) {
            ScrollMetrics(paletteScroll, session.availableElementTypes().size, paletteVisibleRows())
        } else {
            ScrollMetrics(elementScroll, session.state.elements.size, elementVisibleRows())
        }
        SideTab.PROPERTIES -> if (layoutEditorOpen) {
            selectedLayoutFields()?.let { ScrollMetrics(layoutScroll, it.size, layoutVisibleRows()) }
        } else if (sealedEditorPath != null) {
            selectedSealedProperties()?.let { ScrollMetrics(sealedEditorScroll, it.size, sealedVisibleRows()) }
        } else {
            selectedProperties()?.let { ScrollMetrics(propertyScroll, it.size, propertyVisibleRows()) }
        }
        SideTab.PREVIEW_STATE -> {
            session.previewStateFields().let {
                ScrollMetrics(previewStateScroll, it.size, previewStateVisibleRows())
            }
        }
    }?.takeIf { it.visibleRows > 0 && it.itemCount > it.visibleRows }

    private fun renderScrollBar(guiGraphics: GuiGraphics, metrics: ScrollMetrics) {
        val top = previewY + TAB_HEIGHT + 4
        val bottom = height - 8
        val trackHeight = (bottom - top).coerceAtLeast(1)
        val thumbHeight = ceil(trackHeight * (metrics.visibleRows.toDouble() / metrics.itemCount))
            .toInt()
            .coerceAtLeast(12)
            .coerceAtMost(trackHeight)
        val maxOffset = (metrics.itemCount - metrics.visibleRows).coerceAtLeast(1)
        val thumbTravel = trackHeight - thumbHeight
        val thumbTop = top + (thumbTravel * metrics.offset / maxOffset)
        guiGraphics.fill(width - 6, top, width - 4, bottom, 0xFF303030.toInt())
        guiGraphics.fill(width - 6, thumbTop, width - 4, thumbTop + thumbHeight, 0xFF909090.toInt())
    }

    private fun isOverDivider(mouseX: Double, mouseY: Double): Boolean =
        mouseY >= previewY && mouseY <= height &&
            mouseX >= sideX - DIVIDER_HIT_WIDTH && mouseX <= sideX

    private companion object {
        const val DEFAULT_SIDE_WIDTH = 260
        const val MIN_SIDE_WIDTH = 190
        const val MAX_SIDE_WIDTH = 420
        const val MIN_PREVIEW_WIDTH = 160
        const val SCREEN_PADDING = 8
        const val PANEL_GAP = 8
        const val PANEL_INSET = 4
        const val DIVIDER_HALF_WIDTH = 4
        const val DIVIDER_HIT_WIDTH = 8
        const val TAB_HEIGHT = 24
        const val ROW_HEIGHT = 22
        const val PROPERTY_ROW_HEIGHT = 34
        const val PROPERTY_WIDGET_HEIGHT = 20
        const val RANGE_WIDGET_GAP = 2
        const val RANGE_INPUT_MIN_WIDTH = 42
        const val RANGE_INPUT_MAX_WIDTH = 64
        const val MIN_RANGE_SLIDER_WIDTH = 30
        const val PREVIEW_PRESET_WIDGET_Y = 42
        const val PREVIEW_PRESET_APPLY_WIDTH = 52
        const val PREVIEW_FIELD_START_Y = 70
        const val DOUBLE_CLICK_MILLIS = 250L
        const val MODAL_WIDTH = 320
        const val CONFIRM_MODAL_HEIGHT = 116
        const val SCENE_PICKER_MAX_HEIGHT = 330
        const val MODAL_Z = 1_000f
        const val PREVIEW_HINT_SCALE = 2f
        const val PREVIEW_HINT_COLOR = 0xA0FFFFFF.toInt()
        const val SELECTION_COLOR = 0xFFFFFFFF.toInt()
        const val SCREEN_ANCHOR_COLOR = 0xFF55DDFF.toInt()
        const val ELEMENT_ANCHOR_COLOR = 0xFFFFCC33.toInt()
        const val OFFSET_COLOR = 0xAAFFFFFF.toInt()
        const val SCALE_HANDLE_COLOR = 0xFFFFFFFF.toInt()
        const val SCALE_HANDLE_RADIUS = 2
        const val SCALE_HANDLE_HIT_RADIUS = 5f
        const val MIN_UNIFORM_SCALE = 0.05f
        const val MAX_UNIFORM_SCALE = 10f
    }
}
