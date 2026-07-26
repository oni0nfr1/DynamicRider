package io.github.oni0nfr1.dynamicrider.client.hud.elements.speedometer

import io.github.oni0nfr1.dynamicrider.client.graphics.render.shape.drawSpeed7Seg
import io.github.oni0nfr1.dynamicrider.client.graphics.render.shape.fillRoundedTrapezoid
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudColor
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudElementInfo
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudLayout
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HexColorSerdes
import io.github.oni0nfr1.dynamicrider.client.hud.state.SpeedKartState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

class JiuStyleSpdMeter(
    spec: Spec,
    context: HudSceneContext<SpeedKartState>,
    parent: ElementHolder,
) : HudElementImpl<SpeedKartState>(spec.layout, context, parent) {
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

    val speed: Int
        get() = context.kartState.speed.toInt()

    private var glow: Boolean = false
        set(value) {
            if (field == value) return
            animationTime = if (value) context.clock.currentTimeMillis() else null
            field = value
        }

    private var animationTime: Long? = null

    private val animationProgress: Float
        get() = animationTime?.let { startMillis ->
            ((context.clock.currentTimeMillis() - startMillis) / (animationLengthSec * 1000f)).coerceIn(0f, 1f)
        } ?: 0f

    override val width: Int = WIDTH
    override val height: Int = HEIGHT

    override fun updateLayout() {
        syncGlow()
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

    @Serializable
    @SerialName("JIU_TACHOMETER_SIMPLE")
    @HudElementInfo(category = "speedometer")
    data class Spec(
        @HudLayout
        override val layout: HudLayoutSpec = HudLayoutSpec(),
        val tachometerBackgroundScale: Float = 1.25f,
        val animationLengthSec: Float = 0.5f,
        val glowThreshold: Int = 100,
        @HudColor
        @Serializable(with = HexColorSerdes::class)
        val normalDigitColor: Int = 0xFFE8E08A.toInt(),
        @HudColor
        @Serializable(with = HexColorSerdes::class)
        val glowDigitColor: Int = 0xFF00FFFF.toInt(),
        @HudColor
        @Serializable(with = HexColorSerdes::class)
        val offDigitColor: Int = 0x40000000,
        val unitText: String = "km/h",
        @HudColor
        @Serializable(with = HexColorSerdes::class)
        val slotOverlayColor: Int = 0x40000000,
    ) : HudElementSpec<JiuStyleSpdMeter, SpeedKartState> {
        override fun requiredStateClass(): Class<out SpeedKartState> = SpeedKartState::class.java

        override fun create(context: HudSceneContext<SpeedKartState>, parent: ElementHolder) =
            JiuStyleSpdMeter(this, context, parent)
    }

}
