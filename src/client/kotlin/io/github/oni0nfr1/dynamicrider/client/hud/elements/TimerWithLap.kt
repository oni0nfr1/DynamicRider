package io.github.oni0nfr1.dynamicrider.client.hud.elements

import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.graphics.textWithDynriderFont
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.Timer
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.rider.Millis
import io.github.oni0nfr1.dynamicrider.client.rider.RaceTime
import io.github.oni0nfr1.dynamicrider.client.util.milliseconds
import io.github.oni0nfr1.dynamicrider.client.util.minutes
import io.github.oni0nfr1.dynamicrider.client.util.seconds
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import kotlin.math.max

class TimerWithLap(
    stateManager: HudStateManager,
    composer: TimerWithLap.() -> Unit
) : HudElementImpl<TimerWithLap>(stateManager, composer), Timer {

    override var time: RaceTime = RaceTime()

    var bestTimeTotalMillis: Millis = 0

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

        val timeValueText = formatTime(time.interpolatedTotalMillis)
        val bestValueText = formatTime(bestTimeTotalMillis)

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

        val timeValueText = formatTime(time.interpolatedTotalMillis)
        val bestValueText = formatTime(bestTimeTotalMillis)

        val labelColor = withSameAlpha(txtColor, 0x00B0B0B0) // TIME/BEST 라벨용 약간 회색
        val valueColor = txtColor

        var cursorYpx = paddingTopPx

        val poseStack = guiGraphics.pose()
        val bigLapHeightPx = (fontRenderer.lineHeight * LAP_SCALE)
        val lapMainWidthPx = (fontRenderer.width(lapMainText) * LAP_SCALE)

        poseStack.pushPose()
        poseStack.translate(paddingLeftPx.toDouble(), cursorYpx.toDouble(), 0.0)
        poseStack.scale(LAP_SCALE, LAP_SCALE, 1.0f)
        guiGraphics.textWithDynriderFont(0, 0, valueColor, lapMainText, true)
        poseStack.popPose()

        val suffixXpx = paddingLeftPx + lapMainWidthPx.toInt() + LAP_SUFFIX_GAP_PX
        val suffixHeightPx = (fontRenderer.lineHeight * LAP_SUFFIX_SCALE).toInt()
        val suffixYpx = cursorYpx + bigLapHeightPx.toInt() - suffixHeightPx // 아래쪽 정렬

        poseStack.pushPose()
        poseStack.translate(suffixXpx.toDouble(), suffixYpx.toDouble(), 0.0)
        poseStack.scale(LAP_SUFFIX_SCALE, LAP_SUFFIX_SCALE, 1.0f)
        guiGraphics.textWithDynriderFont(0, 0, valueColor, lapSuffixText, true)
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
        guiGraphics.textWithDynriderFont(xPx, yPx, labelColor, labelText, true)
        val valueStartXpx = xPx + fontRenderer.width(labelText)
        guiGraphics.textWithDynriderFont(valueStartXpx, yPx, valueColor, valueText, true)
    }

    private fun formatTime(
        millis: Millis,
    ): String {
        val minutes = millis.minutes
        val seconds = millis.seconds
        val milliseconds  = millis.milliseconds

        val minuteText = minutes.toString().padStart(2, '0')
        val secondText = seconds.toString().padStart(2, '0')
        val milliText  = milliseconds.toString().padStart(3, '0')

        return "$minuteText:$secondText:$milliText"
    }

    private fun withSameAlpha(baseArgb: Int, rgb24: Int): Int {
        val alpha = (baseArgb ushr 24) and 0xFF
        return (alpha shl 24) or (rgb24 and 0x00FFFFFF)
    }
}
