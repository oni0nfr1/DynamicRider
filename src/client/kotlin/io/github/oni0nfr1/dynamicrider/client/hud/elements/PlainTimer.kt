package io.github.oni0nfr1.dynamicrider.client.hud.elements

import io.github.oni0nfr1.dynamicrider.client.graphics.textWithDynriderFont
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
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

class PlainTimer(
    stateManager: HudStateManager,
    composer: PlainTimer.() -> Unit
) : HudElementImpl<PlainTimer>(stateManager, composer), Timer {

    override var time: RaceTime = RaceTime()

    var minWidth: Int = 100
    var txtColor: Int = 0xffffffff.toInt()
    var fontScale: Float = 2.0f

    var boxColor = 0x80000000.toInt()

    var hide: Boolean = false

    private companion object {
        const val PADDING_PX = 6
    }

    override fun resolveSize() {
        val minecraftClient = Minecraft.getInstance()
        val fontRenderer = minecraftClient.font

        val timeValueText = formatTime(time.interpolatedTotalMillis)
        val timeLabelText = "TIME / "

        val timeLineWidthPx = (fontRenderer.width(timeLabelText) + fontRenderer.width(timeValueText)) * fontScale
        val contentWidthPx = timeLineWidthPx

        val contentHeightPx = fontRenderer.lineHeight * fontScale
        val finalWidthPx = max(minWidth, contentWidthPx.toInt() + (PADDING_PX * 2))
        val finalHeightPx = contentHeightPx.toInt() + (PADDING_PX * 2)

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

        val timeValueText = formatTime(time.interpolatedTotalMillis)

        val labelColor = withSameAlpha(txtColor, 0x00B0B0B0) // TIME 라벨용 약간 회색
        val valueColor = txtColor

        val contentHeightPx = size.y - (PADDING_PX * 2)
        val scaledLineHeight = fontRenderer.lineHeight * fontScale
        val cursorYpx = PADDING_PX + ((contentHeightPx - scaledLineHeight) / 2).toInt()

        guiGraphics.fill(0, 0, size.x, size.y, boxColor)

        val pose = guiGraphics.pose()
        pose.pushPose()
        pose.translate(paddingLeftPx.toDouble(), cursorYpx.toDouble(), 0.0)
        pose.scale(fontScale, fontScale, 1.0f)
        drawLabelAndValue(
            guiGraphics = guiGraphics,
            fontRenderer = fontRenderer,
            labelText = "TIME | ",
            valueText = timeValueText,
            xPx = 0,
            yPx = 0,
            labelColor = labelColor,
            valueColor = valueColor
        )
        pose.popPose()
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
