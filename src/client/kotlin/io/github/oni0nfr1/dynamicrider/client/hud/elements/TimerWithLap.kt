package io.github.oni0nfr1.dynamicrider.client.hud.elements

import io.github.oni0nfr1.dynamicrider.client.hud.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.Timer
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import kotlin.math.max

class TimerWithLap(
    stateManager: HudStateManager,
    composer: TimerWithLap.() -> Unit
) : HudElementImpl<TimerWithLap>(stateManager, composer), Timer {

    override var minutes: Int = 0
    override var seconds: Int = 0
    override var milliseconds: Int = 0

    var bestTimeMinutes: Int = 0
    var bestTimeSeconds: Int = 0
    var bestTimeMilliseconds: Int = 0

    var currentLap: Int = 1
    var maxLap: Int? = null

    var minWidth: Int = 100
    var txtColor: Int = 0xffffffff.toInt()
    var hide: Boolean = false

    private companion object {
        const val PADDING_PX = 6
        const val LINE_GAP_PX = 2
        const val LAP_SUFFIX_GAP_PX = 1
        const val AFTER_LAP_GAP_PX = 4

        const val LAP_SCALE = 2.2f
        const val LAP_SUFFIX_SCALE = 1.35f
    }

    override fun resolveSize() {
        val minecraftClient = Minecraft.getInstance()
        val fontRenderer = minecraftClient.font

        val lapMainText = currentLap.coerceAtLeast(0).toString()
        val lapSuffixText = maxLap?.let { " /${it.coerceAtLeast(0)}" }.orEmpty()

        val timeValueText = formatTime(minutes, seconds, milliseconds)
        val bestValueText = formatTime(bestTimeMinutes, bestTimeSeconds, bestTimeMilliseconds)

        val timeLabelText = "TIME / "
        val bestLabelText = "BEST / "

        val lapMainWidthPx = (fontRenderer.width(lapMainText) * LAP_SCALE)
        val lapSuffixWidthPx = (fontRenderer.width(lapSuffixText) * LAP_SUFFIX_SCALE)
        val lapLineWidthPx = lapMainWidthPx + LAP_SUFFIX_GAP_PX + lapSuffixWidthPx

        val timeLineWidthPx = fontRenderer.width(timeLabelText) + fontRenderer.width(timeValueText)
        val bestLineWidthPx = fontRenderer.width(bestLabelText) + fontRenderer.width(bestValueText)

        val contentWidthPx = max(
            lapLineWidthPx.toInt(),
            max(timeLineWidthPx, bestLineWidthPx)
        )

        val bigLapHeightPx = (fontRenderer.lineHeight * LAP_SCALE).toInt()
        val contentHeightPx =
            bigLapHeightPx +
                    AFTER_LAP_GAP_PX +
                    fontRenderer.lineHeight + LINE_GAP_PX +
                    fontRenderer.lineHeight

        val finalWidthPx = max(minWidth, contentWidthPx + (PADDING_PX * 2))
        val finalHeightPx = contentHeightPx + (PADDING_PX * 2)

        setSize(finalWidthPx, finalHeightPx)
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        if (hide) return

        val minecraftClient = Minecraft.getInstance()
        val fontRenderer = minecraftClient.font

        val paddingLeftPx = PADDING_PX
        val paddingTopPx = PADDING_PX

        val lapMainText = currentLap.coerceAtLeast(0).toString()
        val lapSuffixText = maxLap?.let { " /${it.coerceAtLeast(0)}" } ?: "Lap"

        val timeValueText = formatTime(minutes, seconds, milliseconds)
        val bestValueText = formatTime(bestTimeMinutes, bestTimeSeconds, bestTimeMilliseconds)

        val labelColor = withSameAlpha(txtColor, 0x00B0B0B0) // TIME/BEST 라벨용 약간 회색
        val valueColor = txtColor

        var cursorYpx = paddingTopPx

        val poseStack = guiGraphics.pose()
        val bigLapHeightPx = (fontRenderer.lineHeight * LAP_SCALE)
        val lapMainWidthPx = (fontRenderer.width(lapMainText) * LAP_SCALE)

        poseStack.pushPose()
        poseStack.translate(paddingLeftPx.toDouble(), cursorYpx.toDouble(), 0.0)
        poseStack.scale(LAP_SCALE, LAP_SCALE, 1.0f)
        guiGraphics.drawString(fontRenderer, lapMainText, 0, 0, valueColor, true)
        poseStack.popPose()

        val suffixXpx = paddingLeftPx + lapMainWidthPx.toInt() + LAP_SUFFIX_GAP_PX
        val suffixHeightPx = (fontRenderer.lineHeight * LAP_SUFFIX_SCALE).toInt()
        val suffixYpx = cursorYpx + bigLapHeightPx.toInt() - suffixHeightPx // 아래쪽 정렬

        poseStack.pushPose()
        poseStack.translate(suffixXpx.toDouble(), suffixYpx.toDouble(), 0.0)
        poseStack.scale(LAP_SUFFIX_SCALE, LAP_SUFFIX_SCALE, 1.0f)
        guiGraphics.drawString(fontRenderer, lapSuffixText, 0, 0, valueColor, true)
        poseStack.popPose()

        cursorYpx += bigLapHeightPx.toInt() + AFTER_LAP_GAP_PX

        drawLabelAndValue(
            guiGraphics = guiGraphics,
            fontRenderer = fontRenderer,
            labelText = "TIME / ",
            valueText = timeValueText,
            xPx = paddingLeftPx,
            yPx = cursorYpx,
            labelColor = labelColor,
            valueColor = valueColor
        )
        cursorYpx += fontRenderer.lineHeight + LINE_GAP_PX

        drawLabelAndValue(
            guiGraphics = guiGraphics,
            fontRenderer = fontRenderer,
            labelText = "BEST / ",
            valueText = bestValueText,
            xPx = paddingLeftPx,
            yPx = cursorYpx,
            labelColor = labelColor,
            valueColor = valueColor
        )
    }

    private fun drawLabelAndValue(
        guiGraphics: GuiGraphics,
        fontRenderer: Font,
        labelText: String,
        valueText: String,
        xPx: Int,
        yPx: Int,
        labelColor: Int,
        valueColor: Int
    ) {
        guiGraphics.drawString(fontRenderer, labelText, xPx, yPx, labelColor, true)
        val valueStartXpx = xPx + fontRenderer.width(labelText)
        guiGraphics.drawString(fontRenderer, valueText, valueStartXpx, yPx, valueColor, true)
    }

    private fun formatTime(
        minuteValue: Int,
        secondValue: Int,
        millisecondValue: Int
    ): String {
        val safeMinutes = minuteValue.coerceAtLeast(0)
        val safeSeconds = secondValue.coerceIn(0, 59)
        val safeMillis = millisecondValue.coerceIn(0, 999)

        val minuteText = safeMinutes.toString().padStart(2, '0')
        val secondText = safeSeconds.toString().padStart(2, '0')
        val milliText = safeMillis.toString().padStart(3, '0')

        return "$minuteText:$secondText:$milliText"
    }

    private fun withSameAlpha(baseArgb: Int, rgb24: Int): Int {
        val alpha = (baseArgb ushr 24) and 0xFF
        return (alpha shl 24) or (rgb24 and 0x00FFFFFF)
    }
}