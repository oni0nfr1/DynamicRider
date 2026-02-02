package io.github.oni0nfr1.dynamicrider.client.hud.elements.speedmeter

import io.github.oni0nfr1.dynamicrider.client.graphics.textWithDynriderFont
import io.github.oni0nfr1.dynamicrider.client.hud.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.SpeedMeter
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import kotlin.math.ceil
import kotlin.math.max

class PlainSpeedMeter(
    manager: HudStateManager,
    composer: PlainSpeedMeter.() -> Unit
): HudElementImpl<PlainSpeedMeter>(manager, composer), SpeedMeter {

    override var speed = 0
        set(value) {
            cachedStr = "$value km/h"
            field = value
        }

    private var cachedStr = "$speed km/h" // 매 프레임마다 문자열 재생성 막기 최적화

    private var boxSizeX = 0
    private var boxSizeY = 0

    private var boxPadding = 8

    var minWidth = 100
    var bgColor = 0x80000000.toInt()
    var txtColor = 0xffffffff.toInt()
    var fontScale = 1.5f

    override fun resolveSize() {
        val font = Minecraft.getInstance().font
        val txtWidth = font.width(cachedStr) * fontScale
        val txtHeight = font.lineHeight * fontScale

        boxSizeX = max(ceil(txtWidth + 2 * boxPadding).toInt(), minWidth)
        boxSizeY = ceil(txtHeight + 2 * boxPadding).toInt()
        setSize(boxSizeX, boxSizeY)
    }

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        guiGraphics.fill(0, 0, boxSizeX, boxSizeY, bgColor)
        val pose = guiGraphics.pose()
        pose.pushPose()
        pose.translate(boxPadding.toFloat(), boxPadding.toFloat(), 0f)
        pose.scale(fontScale, fontScale, 1f)
        guiGraphics.textWithDynriderFont(0, 0, txtColor, cachedStr)
        pose.popPose()
    }
}