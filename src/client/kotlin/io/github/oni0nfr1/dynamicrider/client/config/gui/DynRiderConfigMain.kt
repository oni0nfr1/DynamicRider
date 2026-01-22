package io.github.oni0nfr1.dynamicrider.client.config.gui

import io.github.oni0nfr1.dynamicrider.client.config.DynRiderConfig
import io.github.oni0nfr1.dynamicrider.client.config.DynRiderConfigData
import io.github.oni0nfr1.dynamicrider.client.config.FontStyle
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.CycleButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class DynRiderConfigMain(
    private val parentScreen: Screen,
) : Screen(Component.translatable("dynamicrider.config.main.title")) {
    private var workingIsModEnabled: Boolean = true
    private var workingFontOption: FontStyle = FontStyle.VANILLA

    private lateinit var enableToggleButton: CycleButton<Boolean>
    private lateinit var fontCycleButton: CycleButton<FontStyle>

    override fun init() {
        val currentConfig: DynRiderConfigData = DynRiderConfig.currentData

        workingIsModEnabled = currentConfig.isModEnabled
        workingFontOption = runCatching { FontStyle.valueOf(currentConfig.hudFont) }
            .getOrElse { FontStyle.VANILLA }

        val centerX = this.width / 2
        val rowWidth = 220
        val rowHeight = 20
        val firstRowY = this.height / 4 + 20
        val rowGap = 24

        enableToggleButton = CycleButton.onOffBuilder(workingIsModEnabled)
            .create(
                centerX - rowWidth / 2,
                firstRowY,
                rowWidth,
                rowHeight,
                Component.translatable("dynamicrider.config.enabled")
            ) { _, newValue ->
                workingIsModEnabled = newValue
            }

        fontCycleButton = CycleButton.builder<FontStyle> { option ->
            Component.translatable(option.translationKey)
        }
            .withValues(FontStyle.entries)
            .withInitialValue(workingFontOption)
            .create(
                centerX - rowWidth / 2,
                firstRowY + rowGap,
                rowWidth,
                rowHeight,
                Component.translatable("dynamicrider.config.font")
            ) { _, newValue ->
                workingFontOption = newValue
            }

        val saveButton = Button.builder(Component.translatable("dynamicrider.config.save")) {
            val newConfig = DynRiderConfigData(
                isModEnabled = workingIsModEnabled,
                hudFont = workingFontOption.name,
            )
            DynRiderConfig.save(newConfig)
            DynRiderConfig.apply(newConfig)

            Minecraft.getInstance().setScreen(parentScreen)
        }.bounds(centerX - 110, firstRowY + rowGap * 3, 100, 20).build()

        val cancelButton = Button.builder(Component.translatable("dynamicrider.config.cancel")) {
            Minecraft.getInstance().setScreen(parentScreen)
        }.bounds(centerX + 10, firstRowY + rowGap * 3, 100, 20).build()

        addRenderableWidget(enableToggleButton)
        addRenderableWidget(fontCycleButton)
        addRenderableWidget(saveButton)
        addRenderableWidget(cancelButton)
    }
}