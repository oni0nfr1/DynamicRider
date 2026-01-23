package io.github.oni0nfr1.dynamicrider.client.hud.elements

import io.github.oni0nfr1.dynamicrider.client.hud.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.graphics.textWithDynriderFont
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.Timer
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.rider.Millis
import io.github.oni0nfr1.dynamicrider.client.rider.RaceTime
import io.github.oni0nfr1.dynamicrider.client.util.milliseconds
import io.github.oni0nfr1.dynamicrider.client.util.minutes
import io.github.oni0nfr1.dynamicrider.client.util.seconds
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import kotlin.math.ceil
import kotlin.math.max

private const val TIMER_PLACEHOLDER = "00:00:000"

class PlainTimer(
    manager: HudStateManager,
    composer: PlainTimer.() -> Unit,
): HudElementImpl<PlainTimer>(manager, composer), Timer {

    override var time: RaceTime = RaceTime()
    private val timeStrBuf = charArrayOf('0', '0', ':', '0', '0', ':', '0', '0', '0')

    var boxSizeX = 0
    var boxSizeY = 0

    var boxPadding = 8

    var minWidth = 100
    var bgColor = 0x80000000.toInt()
    var txtColor = 0xffffffff.toInt()
    var fontScale = 1.5f
    var hide = false

    override fun resolveSize() {
        if (hide) {
            setSize(0, 0)
            return
        }
        val font = Minecraft.getInstance().font
        val txtWidth = font.width(TIMER_PLACEHOLDER) * fontScale
        val txtHeight = font.lineHeight * fontScale

        boxSizeX = max(ceil(txtWidth + 2 * boxPadding).toInt(), minWidth)
        boxSizeY = ceil(txtHeight + 2 * boxPadding).toInt()
        setSize(boxSizeX, boxSizeY)
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        if (hide) return
        val timeString = buildTimeString(time.interpolatedTotalMillis)
        guiGraphics.fill(0, 0, boxSizeX, boxSizeY, bgColor)
        val pose = guiGraphics.pose()
        pose.pushPose()
        pose.translate(boxPadding.toFloat(), boxPadding.toFloat(), 0f)
        pose.scale(fontScale, fontScale, 1f)
        guiGraphics.textWithDynriderFont(0, 0, txtColor, timeString)
        pose.popPose()
    }

    private fun buildTimeString(
        millis: Millis
    ): String {
        val mins = millis.minutes.coerceIn(0, 99)
        val secs = millis.seconds.coerceIn(0, 59)
        val millis = millis.milliseconds.coerceIn(0, 999)

        timeStrBuf[0] = digit(mins / 10)
        timeStrBuf[1] = digit(mins % 10)
        timeStrBuf[2] = ':'
        timeStrBuf[3] = digit(secs / 10)
        timeStrBuf[4] = digit(secs % 10)
        timeStrBuf[5] = ':'
        timeStrBuf[6] = digit(millis / 100)
        timeStrBuf[7] = digit((millis / 10) % 10)
        timeStrBuf[8] = digit(millis % 10)
        return String(timeStrBuf)
    }

    private fun digit(value: Int): Char = ('0'.code + value).toChar()
}
