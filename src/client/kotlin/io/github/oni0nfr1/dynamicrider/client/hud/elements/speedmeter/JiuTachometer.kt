package io.github.oni0nfr1.dynamicrider.client.hud.elements.speedmeter

import io.github.oni0nfr1.dynamicrider.client.graphics.drawSpeed7Seg
import io.github.oni0nfr1.dynamicrider.client.graphics.fillRoundedTrapezoid
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.SpeedMeter
import io.github.oni0nfr1.skid.client.api.engine.SpeedEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

class JiuTachometer(
    spec: JiuTachometerSpec,
    kart: KartRef.Specific<SpeedEngine>,
) : HudElementImpl<SpeedEngine>(spec.layout, kart), SpeedMeter {
    private companion object {
        const val HEIGHT = 65
        const val WIDTH = 130
        const val GLOW_FRAMES = 24

        val baseTexture: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath("dynrider", "textures/gui/hud/jiu/base.png")
        val glowSheetTexture: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath("dynrider", "textures/gui/hud/jiu/arc_glow.png")
        val glowBaseTexture: ResourceLocation =
            ResourceLocation.fromNamespaceAndPath("dynrider", "textures/gui/hud/jiu/base_glow.png")
    }

    var tachometerBackgroundScale: Float = spec.tachometerBackgroundScale
    var animationLengthSec: Float = spec.animationLengthSec
    var glowThreshold: Int = spec.glowThreshold
    var normalDigitColor: Int = spec.normalDigitColor
    var glowDigitColor: Int = spec.glowDigitColor
    var offDigitColor: Int = spec.offDigitColor
    var unitText: String = spec.unitText
    var slotOverlayColor: Int = spec.slotOverlayColor

    override val speed: Int
        get() = kart.accessEngine { engine ->
            engine.tachometer?.speed?.toInt()
        } ?: 0

    private var glow: Boolean = false
        set(value) {
            if (field == value) return
            animationTime = if (value) System.currentTimeMillis() else null
            field = value
        }

    private var animationTime: Long? = null

    private val animationProgress: Float
        get() = animationTime?.let { startMillis ->
            ((System.currentTimeMillis() - startMillis) / (animationLengthSec * 1000f)).coerceIn(0f, 1f)
        } ?: 0f

    override fun resolveSize() {
        syncGlow()
        setSize(WIDTH, HEIGHT)
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker,
    ) {
        syncGlow()

        val destW = (WIDTH * tachometerBackgroundScale).toInt()
        val destH = (HEIGHT * tachometerBackgroundScale).toInt()
        val drawX = (WIDTH - destW) / 2
        val drawY = HEIGHT - destH

        val backgroundTexture = if (animationProgress != 1f) baseTexture else glowBaseTexture
        guiGraphics.blit(
            RenderType::guiTextured,
            backgroundTexture,
            drawX,
            drawY,
            0f,
            0f,
            destW,
            destH,
            WIDTH,
            HEIGHT,
            WIDTH,
            HEIGHT,
        )

        if (glow) {
            val frameIndex = ((animationProgress * (GLOW_FRAMES - 1)).toInt()).coerceIn(0, GLOW_FRAMES - 1)
            guiGraphics.blit(
                RenderType::guiTextured,
                glowSheetTexture,
                drawX,
                drawY,
                0f,
                (frameIndex * HEIGHT).toFloat(),
                destW,
                destH,
                WIDTH,
                HEIGHT,
                WIDTH,
                HEIGHT * GLOW_FRAMES,
            )
        }

        guiGraphics.drawSpeed7Seg(
            xCenter = WIDTH / 2f,
            yTop = 53f,
            speed = speed,
            onArgb = if (glow) glowDigitColor else normalDigitColor,
            offArgb = offDigitColor,
            unitText = unitText,
        )

        guiGraphics.fillRoundedTrapezoid(
            x = WIDTH / 2f - 25f,
            y = 25f,
            topWidth = 50f,
            bottomWidth = 44f,
            height = 12f,
            cornerRadius = 2f,
            argb = slotOverlayColor,
        )
    }

    private fun syncGlow() {
        glow = speed >= glowThreshold
    }
}
