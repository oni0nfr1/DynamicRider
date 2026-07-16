package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudElementInspectionResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditableProperty
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorActionResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorPersistenceResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorSession
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorSessionFactory
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorSessionOpenResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorState
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.HudPropertyEditorType
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
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import kotlin.math.ceil
import kotlin.math.abs
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
    private var layoutEditorOpen = false
    private var elementDrag: ElementDrag? = null
    private var scaleDrag: ScaleDrag? = null
    private var modal: EditorModal? = null
    private var editorWidgets: List<AbstractWidget> = emptyList()
    private var modalWidgets: List<AbstractWidget> = emptyList()
    private val propertyErrors = mutableMapOf<Pair<String, HudPropertyPath>, String>()

    private var previewX = 8
    private var previewY = 38
    private var previewWidth = 1
    private var previewHeight = 1
    private var sideX = 1
    private var sideWidth = 1

    override fun init() {
        calculateLayout()
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
        buildToolbar()
        buildTabs()
        when (activeTab) {
            SideTab.ELEMENTS -> buildElementsTab()
            SideTab.PROPERTIES -> buildPropertiesTab()
            SideTab.PREVIEW_STATE -> Unit
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
        renderEditorChrome(guiGraphics, mouseX, mouseY)
        renderPreview(guiGraphics)
        renderSideContent(guiGraphics)
        if (modal == null) {
            super.render(guiGraphics, mouseX, mouseY, partialTick)
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
        if (modal != null) return super.mouseClicked(mouseX, mouseY, button)
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
            val localX = (mouseX - previewX).toFloat()
            val localY = (mouseY - previewY).toFloat()
            val guides = previewGuides()
            val selectedGuide = session.state.selectedElementId
                ?.let { selectedId -> guides.firstOrNull { it.elementId == selectedId } }
            if (selectedGuide != null && isOverScaleHandle(selectedGuide, localX, localY)) {
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
                } else {
                    val count = selectedProperties()?.size ?: 0
                    propertyScroll = clampScroll(propertyScroll + direction, count, propertyVisibleRows())
                }
            }
            SideTab.PREVIEW_STATE -> return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
        }
        rebuildWidgets()
        return true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (modal != null) return super.keyPressed(keyCode, scanCode, modifiers)
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
        previewViewport.resize(previewWidth, previewHeight)
    }

    private fun buildToolbar() {
        val state = session.state
        addRenderableWidget(toolbarButton(8, "dynamicrider.hud.editor.undo") { session.undo() }.also {
            it.active = state.canUndo
        })
        addRenderableWidget(toolbarButton(72, "dynamicrider.hud.editor.redo") { session.redo() }.also {
            it.active = state.canRedo
        })
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
        val visibleRows = propertyVisibleRows()
        propertyScroll = clampScroll(propertyScroll, properties.size, visibleRows)
        val widgetX = sideX + sideWidth / 2
        val widgetWidth = (sideWidth / 2 - 8).coerceAtLeast(40)
        val firstRowY = previewY + 46
        val factory = HudPropertyWidgetFactory(font)
        properties.drop(propertyScroll).take(visibleRows).forEachIndexed { index, property ->
            val y = firstRowY + index * PROPERTY_ROW_HEIGHT
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
                    layoutScroll = 0
                    rebuildWidgets()
                },
            ).forEach(::addRenderableWidget)
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
        guiGraphics.enableScissor(previewX, previewY, previewX + previewWidth, previewY + previewHeight)
        guiGraphics.pose().pushPose()
        guiGraphics.pose().translate(previewX.toFloat(), previewY.toFloat(), 0f)
        session.previewScene.draw(guiGraphics, Minecraft.getInstance().deltaTracker)
        renderSelectionOverlay(guiGraphics)
        guiGraphics.pose().popPose()
        guiGraphics.disableScissor()
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
            SideTab.PREVIEW_STATE -> guiGraphics.drawWordWrap(
                font,
                Component.translatable("dynamicrider.hud.editor.preview_state.pending"),
                sideX + 6,
                contentY,
                sideWidth - 12,
                0xFFBBBBBB.toInt(),
            )
        }
        currentScrollMetrics()?.let { renderScrollBar(guiGraphics, it) }
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
        val anchorX = (mouseX - previewX).toFloat() - drag.grabOffsetX
        val anchorY = (mouseY - previewY).toFloat() - drag.grabOffsetY
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

    private fun renderSelectionOverlay(guiGraphics: GuiGraphics) {
        val selectedId = session.state.selectedElementId ?: return
        val guide = previewGuides().firstOrNull { it.elementId == selectedId } ?: return
        val left = guide.bounds.left.roundToInt()
        val top = guide.bounds.top.roundToInt()
        val right = guide.bounds.right.roundToInt()
        val bottom = guide.bounds.bottom.roundToInt()
        guiGraphics.renderOutline(
            left,
            top,
            (right - left).coerceAtLeast(1),
            (bottom - top).coerceAtLeast(1),
            SELECTION_COLOR,
        )

        val screenX = guide.screenAnchorX.roundToInt()
        val screenY = guide.screenAnchorY.roundToInt()
        val elementX = guide.elementAnchorX.roundToInt()
        val elementY = guide.elementAnchorY.roundToInt()
        drawAnchorCross(guiGraphics, screenX, screenY, SCREEN_ANCHOR_COLOR, 4)
        drawAnchorLine(guiGraphics, screenX.toFloat(), screenY.toFloat(), elementX.toFloat(), elementY.toFloat())
        drawAnchorCross(guiGraphics, elementX, elementY, ELEMENT_ANCHOR_COLOR, 3)
        val (handleX, handleY) = guide.scaleHandle()
        guiGraphics.fill(
            handleX.roundToInt() - SCALE_HANDLE_RADIUS,
            handleY.roundToInt() - SCALE_HANDLE_RADIUS,
            handleX.roundToInt() + SCALE_HANDLE_RADIUS + 1,
            handleY.roundToInt() + SCALE_HANDLE_RADIUS + 1,
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
        val pointerX = (mouseX - previewX).toFloat() - drag.anchorX
        val pointerY = (mouseY - previewY).toFloat() - drag.anchorY
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

    private fun isOverScaleHandle(guide: HudPreviewElementGuide, x: Float, y: Float): Boolean {
        val (handleX, handleY) = guide.scaleHandle()
        return abs(x - handleX) <= SCALE_HANDLE_HIT_RADIUS && abs(y - handleY) <= SCALE_HANDLE_HIT_RADIUS
    }

    private fun previewGuides(): List<HudPreviewElementGuide> =
        HudPreviewElementGuideCalculator.calculate(session.previewScene)

    private fun isInsidePreview(mouseX: Double, mouseY: Double): Boolean =
        mouseX >= previewX && mouseX < previewX + previewWidth &&
            mouseY >= previewY && mouseY < previewY + previewHeight

    private fun selectedProperties() = session.state.selectedElementId
        ?.let(session::inspectElement)
        ?.let { (it as? HudElementInspectionResult.Inspected)?.model?.properties }

    private fun selectedLayoutFields(): List<HudEditableProperty>? = selectedProperties()
        ?.firstOrNull { it.editor is HudPropertyEditorType.LayoutEditor }
        ?.let(HudLayoutEditorModel::fields)

    private fun elementVisibleRows(): Int = ((height - (previewY + 26) - 58) / ROW_HEIGHT).coerceAtLeast(0)

    private fun paletteVisibleRows(): Int = ((height - (previewY + 26) - 34) / ROW_HEIGHT).coerceAtLeast(0)

    private fun propertyVisibleRows(): Int = ((height - (previewY + 28) - 24) / PROPERTY_ROW_HEIGHT).coerceAtLeast(0)

    private fun layoutVisibleRows(): Int = ((height - (previewY + 52) - 8) / PROPERTY_ROW_HEIGHT).coerceAtLeast(0)

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
        } else {
            selectedProperties()?.let { ScrollMetrics(propertyScroll, it.size, propertyVisibleRows()) }
        }
        SideTab.PREVIEW_STATE -> null
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
        const val DOUBLE_CLICK_MILLIS = 250L
        const val MODAL_WIDTH = 320
        const val CONFIRM_MODAL_HEIGHT = 116
        const val SCENE_PICKER_MAX_HEIGHT = 330
        const val MODAL_Z = 1_000f
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
