package io.github.oni0nfr1.dynamicrider.client.hud.elements.speedmeter

import io.github.oni0nfr1.dynamicrider.client.graphics.drawSpeed7Seg
import io.github.oni0nfr1.dynamicrider.client.graphics.fillRoundedTrapezoid
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.Composer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.SpeedMeter
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.util.applyAlphaToColor
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

class JiuTachometer(
    stateManager: HudStateManager,
    composer: Composer<JiuTachometer>
) : HudElementImpl<JiuTachometer>(stateManager, composer), SpeedMeter {

    private companion object {
        const val HEIGHT = 65
        const val WIDTH = 130

        const val GLOW_FRAMES = 24
    }

    var tachometerBackgroundScale = 1.25f
    private val baseTexture = ResourceLocation.fromNamespaceAndPath("dynrider", "textures/gui/hud/jiu/base.png")
    private val glowSheetTexture = ResourceLocation.fromNamespaceAndPath("dynrider", "textures/gui/hud/jiu/arc_glow.png")
    private val glowBaseTexture = ResourceLocation.fromNamespaceAndPath("dynrider", "textures/gui/hud/jiu/base_glow.png")

    override var speed: Int = 0

    var glow = false
        set(value) {
            if (field == value) return
            animationTime = if (value) System.currentTimeMillis() else null
            field = value
        }

    var animationLengthSec = 0.5f
    private var animationTime: Long? = null

    private val animationProgress: Float
        get() = animationTime?.let { startMillis ->
            ((System.currentTimeMillis() - startMillis) / (animationLengthSec * 1000f)).coerceIn(0f, 1f)
        } ?: 0f

    override fun resolveSize() = setSize(WIDTH, HEIGHT)

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        val destW = (WIDTH * tachometerBackgroundScale).toInt()
        val destH = (HEIGHT * tachometerBackgroundScale).toInt()

        val drawX = (WIDTH - destW) / 2
        val drawY = HEIGHT - destH

        if (animationProgress != 1f) {
            guiGraphics.blit(
                RenderType::guiTextured,
                baseTexture,
                drawX, drawY,
                0f, 0f,
                destW, destH,
                WIDTH, HEIGHT,
                WIDTH, HEIGHT
            )
        } else {
            guiGraphics.blit(
                RenderType::guiTextured,
                glowBaseTexture,
                drawX, drawY,
                0f, 0f,
                destW, destH,
                WIDTH, HEIGHT,
                WIDTH, HEIGHT
            )
        }
        if (glow) {
            val frameIndex = ((animationProgress * (GLOW_FRAMES - 1)).toInt()).coerceIn(0, GLOW_FRAMES - 1)

            guiGraphics.blit(
                RenderType::guiTextured,
                glowSheetTexture,
                drawX, drawY,
                0f, (frameIndex * HEIGHT).toFloat(),
                destW, destH,
                WIDTH, HEIGHT,
                WIDTH, HEIGHT * GLOW_FRAMES
            )
        }

        guiGraphics.drawSpeed7Seg(
            WIDTH / 2f,
            53f,
            speed,
            if (glow) 0xFF00FFFF.toInt() else 0xFFE8E08A.toInt(),
            0x40000000,
            unitText = "km/h"
        )

        val centerX = WIDTH / 2f
        val slotTopWidth = 50f
        val slotBottomWidth = 44f
        guiGraphics.fillRoundedTrapezoid(
            centerX - slotTopWidth / 2f,
            25f,
            slotTopWidth,
            slotBottomWidth,
            12f,
            2f,
            0xA0000000.toInt().applyAlphaToColor(0x40),
        )
    }
}