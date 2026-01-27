package io.github.oni0nfr1.dynamicrider.client.hud.elements

import io.github.oni0nfr1.dynamicrider.client.command.DebugVariables
import io.github.oni0nfr1.dynamicrider.client.graphics.drawJiuEngineArcGlow
import io.github.oni0nfr1.dynamicrider.client.graphics.drawJiuEngineArcGlowEmpty
import io.github.oni0nfr1.dynamicrider.client.graphics.drawSpeed7Seg
import io.github.oni0nfr1.dynamicrider.client.graphics.fillRoundedTrapezoid
import io.github.oni0nfr1.dynamicrider.client.hud.impl.Composer
import io.github.oni0nfr1.dynamicrider.client.hud.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.SpeedMeter
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.util.applyAlphaToColor
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import java.lang.System.currentTimeMillis
import kotlin.math.PI

class JiuTachometer(
    stateManager: HudStateManager,
    composer: Composer<JiuTachometer>
): HudElementImpl<JiuTachometer>(stateManager, composer), SpeedMeter {
    private companion object {
        const val HEIGHT = 65
        const val WIDTH = 130
    }

    override var speed: Int = 0

    var defaultColor = 0xA0000000.toInt()
    var glowColor = 0xFF00FFFF.toInt()
    var glow = false
        set(value) {
            if (field == value) return
            animationTime = if (value) currentTimeMillis() else null
            field = value
        }

    var animationLengthSec = 0.5f
    var animationTime: Long? = null
        private set
    val animationProgress: Float
        get() = animationTime?.let{
            ((currentTimeMillis() - it) / animationLengthSec / 1000).coerceIn(0f, 1f)
        } ?: 0f

    override fun resolveSize() {
        setSize(WIDTH, HEIGHT)
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        val fadeTopY = 20f
        val fadeBottomY = 25f
        val arcThickness = 4f

        val arcAngle = PI.toFloat() / 2f
        val startRadLeft = PI.toFloat()
        val endRadLeft = startRadLeft + arcAngle * animationProgress
        val endRadLeftStatic = startRadLeft + arcAngle

        val startRadRight = PI.toFloat() * 2f
        val endRadRight = startRadRight - arcAngle * animationProgress
        val endRadRightStatic = startRadRight - arcAngle

        val radius = WIDTH / 2f - 10

        if (glow) {
            guiGraphics.drawJiuEngineArcGlow(
                WIDTH / 2f, HEIGHT.toFloat(),
                radius,
                arcThickness, 3f, radius / 2,
                startRadLeft, (endRadLeft - PI / 36f).toFloat(), endRadLeft,
                glowColor,
                1,
                fadeTopY, fadeBottomY,
            )

            guiGraphics.drawJiuEngineArcGlow(
                WIDTH / 2f, HEIGHT.toFloat(),
                radius,
                arcThickness, 3f, radius / 2,
                startRadRight, (endRadRight + PI / 36f).toFloat(), endRadRight,
                glowColor,
                1,
                fadeTopY, fadeBottomY,
            )
        }

        guiGraphics.drawJiuEngineArcGlowEmpty(
            WIDTH / 2f, HEIGHT.toFloat(),
            radius,
            arcThickness, 3f, radius / 2,
            startRadLeft, endRadLeftStatic,
            defaultColor,
            1,
            fadeTopY, fadeBottomY,
        )

        guiGraphics.drawJiuEngineArcGlowEmpty(
            WIDTH / 2f, HEIGHT.toFloat(),
            radius,
            arcThickness, 3f,radius / 2,
            startRadRight, endRadRightStatic,
            defaultColor,
            1,
            fadeTopY, fadeBottomY,
        )

        guiGraphics.drawSpeed7Seg(
            WIDTH / 2f,
            53f,
            speed,
            if (glow) glowColor else 0xFFE8E08A.toInt(),
            0x40000000,
            unitText = "km/h"
        )

        val centerX = WIDTH / 2f
        val slotTopWidth = DebugVariables.iconSlotTopWidth.toFloat()
        val slotBottomWidth = DebugVariables.iconSlotBottomWidth.toFloat()
        guiGraphics.fillRoundedTrapezoid(
            centerX - slotTopWidth / 2f,
            25f,
            slotTopWidth,
            slotBottomWidth,
            12f,
            2f,
            if (glow) glowColor.applyAlphaToColor(0x40)
            else defaultColor.applyAlphaToColor(0x40),
        )
    }

}