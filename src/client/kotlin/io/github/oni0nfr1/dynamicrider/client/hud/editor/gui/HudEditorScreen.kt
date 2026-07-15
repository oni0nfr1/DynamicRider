package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudElementInspectionResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.inspector.HudEditableProperty
import io.github.oni0nfr1.dynamicrider.client.hud.editor.property.HudPropertyPath
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorActionResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorPersistenceResult
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorSession
import io.github.oni0nfr1.dynamicrider.client.hud.editor.session.HudEditorState
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import kotlin.math.ceil
import kotlin.math.min

/** 중앙 HUD preview와 탭식 단일 side panel을 제공하는 첫 editor 화면이다. */
class HudEditorScreen(
    private val parentScreen: Screen,
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
    private var confirmRestore = false
    private var status: Component? = null
    private var stateSubscription: AutoCloseable? = null
    private var closing = false
    private var preferredSideWidth: Int? = null
    private var resizingSidePanel = false
    private var lastDividerClickMillis = 0L
    private var elementScroll = 0
    private var paletteScroll = 0
    private var propertyScroll = 0
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
                confirmRestore = false
                Minecraft.getInstance().execute {
                    if (Minecraft.getInstance().screen === this) rebuildWidgets()
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
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        guiGraphics.fill(0, 0, width, height, 0xF0080808.toInt())
        renderEditorChrome(guiGraphics, mouseX, mouseY)
        renderPreview(guiGraphics)
        renderSideContent(guiGraphics)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }

    /** Editor는 자체 배경을 그리므로 vanilla menu blur와 배경 texture를 적용하지 않는다. */
    override fun renderBackground(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) = Unit

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
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
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(
        mouseX: Double,
        mouseY: Double,
        button: Int,
        dragX: Double,
        dragY: Double,
    ): Boolean {
        if (resizingSidePanel && button == 0) {
            preferredSideWidth = width - SCREEN_PADDING - mouseX.toInt() - PANEL_INSET
            rebuildWidgets()
            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (resizingSidePanel && button == 0) {
            resizingSidePanel = false
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
                val count = selectedProperties()?.size ?: 0
                propertyScroll = clampScroll(propertyScroll + direction, count, propertyVisibleRows())
            }
            SideTab.PREVIEW_STATE -> return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
        }
        rebuildWidgets()
        return true
    }

    override fun onClose() {
        if (closing) return
        closing = true
        Minecraft.getInstance().setScreen(parentScreen)
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
        addRenderableWidget(toolbarButton(width - 206, "dynamicrider.hud.editor.save") { save() })
        addRenderableWidget(toolbarButton(width - 142, "dynamicrider.hud.editor.restore") { restore() })
        addRenderableWidget(toolbarButton(width - 70, "gui.done") { onClose() })
    }

    private fun buildTabs() {
        val tabWidth = sideWidth / SideTab.entries.size
        SideTab.entries.forEachIndexed { index, tab ->
            addRenderableWidget(
                Button.builder(Component.translatable(tab.key)) {
                    activeTab = tab
                    paletteOpen = false
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
        guiGraphics.drawCenteredString(font, sceneLabel, width / 2, 12, 0xFFFFFF)
        status?.let { guiGraphics.drawString(font, it, previewX + 4, height - 18, 0xFFFF7777.toInt()) }
    }

    private fun renderPreview(guiGraphics: GuiGraphics) {
        guiGraphics.enableScissor(previewX, previewY, previewX + previewWidth, previewY + previewHeight)
        guiGraphics.pose().pushPose()
        guiGraphics.pose().translate(previewX.toFloat(), previewY.toFloat(), 0f)
        session.previewScene.draw(guiGraphics, Minecraft.getInstance().deltaTracker)
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
        guiGraphics.drawString(font, Component.translatable(inspected.model.nameKey), sideX + 6, contentY, 0xFFFFFFFF.toInt())
        val maxRows = propertyVisibleRows()
        propertyScroll = clampScroll(propertyScroll, inspected.model.properties.size, maxRows)
        inspected.model.properties.drop(propertyScroll).take(maxRows).forEachIndexed { index, property ->
            val y = contentY + 18 + index * PROPERTY_ROW_HEIGHT
            val color = if (property.supported) 0xFFDDDDDD.toInt() else 0xFF777777.toInt()
            guiGraphics.drawString(font, Component.translatable(property.nameKey), sideX + 6, y, color)
            propertyErrors[selectedId to property.path]?.let { message ->
                guiGraphics.drawString(font, message, sideX + 6, y + 21, 0xFFFF6666.toInt())
            }
        }
    }

    private fun toolbarButton(x: Int, key: String, action: () -> Unit): Button =
        Button.builder(Component.translatable(key)) { action() }.bounds(x, 6, 60, 20).build()

    private fun removeSelected() {
        session.state.selectedElementId?.let(session::removeElement)
    }

    private fun moveSelected(offset: Int) {
        val state = session.state
        val id = state.selectedElementId ?: return
        val index = state.elements.indexOfFirst { it.id == id }
        if (index >= 0) session.moveElement(id, index + offset)
    }

    private fun save() {
        status = when (session.save()) {
            is HudEditorPersistenceResult.Saved -> Component.translatable("dynamicrider.hud.editor.saved")
            is HudEditorPersistenceResult.IoFailed -> Component.translatable("dynamicrider.hud.editor.save_failed")
            else -> Component.translatable("dynamicrider.hud.editor.action_failed")
        }
        confirmRestore = false
    }

    private fun restore() {
        val result = session.deleteCustom(discardUnsavedChanges = confirmRestore)
        status = when (result) {
            HudEditorPersistenceResult.DiscardConfirmationRequired -> {
                confirmRestore = true
                Component.translatable("dynamicrider.hud.editor.restore_confirm")
            }
            is HudEditorPersistenceResult.Restored -> {
                confirmRestore = false
                Component.translatable("dynamicrider.hud.editor.restored")
            }
            is HudEditorPersistenceResult.IoFailed -> Component.translatable("dynamicrider.hud.editor.restore_failed")
            is HudEditorPersistenceResult.ResolveFailed -> Component.translatable("dynamicrider.hud.editor.restore_failed")
            else -> Component.translatable("dynamicrider.hud.editor.action_failed")
        }
    }

    private fun updateProperty(
        elementId: String,
        property: HudEditableProperty,
        value: kotlinx.serialization.json.JsonElement,
    ) {
        val key = elementId to property.path
        when (val result = session.updateProperty(elementId, property.path, value)) {
            is HudEditorActionResult.Applied -> {
                propertyErrors.remove(key)
                status = null
            }
            HudEditorActionResult.Unchanged -> {
                propertyErrors.remove(key)
                status = null
                rebuildWidgets()
            }
            is HudEditorActionResult.PropertyRejected -> {
                propertyErrors[key] = result.failure.message
                status = Component.translatable("dynamicrider.hud.editor.property.rejected")
            }
            else -> status = Component.translatable("dynamicrider.hud.editor.action_failed")
        }
    }

    private fun selectedProperties() = session.state.selectedElementId
        ?.let(session::inspectElement)
        ?.let { (it as? HudElementInspectionResult.Inspected)?.model?.properties }

    private fun elementVisibleRows(): Int = ((height - (previewY + 26) - 58) / ROW_HEIGHT).coerceAtLeast(0)

    private fun paletteVisibleRows(): Int = ((height - (previewY + 26) - 34) / ROW_HEIGHT).coerceAtLeast(0)

    private fun propertyVisibleRows(): Int = ((height - (previewY + 28) - 24) / PROPERTY_ROW_HEIGHT).coerceAtLeast(0)

    private fun clampScroll(offset: Int, itemCount: Int, visibleRows: Int): Int =
        offset.coerceIn(0, (itemCount - visibleRows).coerceAtLeast(0))

    private data class ScrollMetrics(
        val offset: Int,
        val itemCount: Int,
        val visibleRows: Int,
    )

    private fun currentScrollMetrics(): ScrollMetrics? = when (activeTab) {
        SideTab.ELEMENTS -> if (paletteOpen) {
            ScrollMetrics(paletteScroll, session.availableElementTypes().size, paletteVisibleRows())
        } else {
            ScrollMetrics(elementScroll, session.state.elements.size, elementVisibleRows())
        }
        SideTab.PROPERTIES -> selectedProperties()?.let {
            ScrollMetrics(propertyScroll, it.size, propertyVisibleRows())
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
        const val DOUBLE_CLICK_MILLIS = 250L
    }
}
