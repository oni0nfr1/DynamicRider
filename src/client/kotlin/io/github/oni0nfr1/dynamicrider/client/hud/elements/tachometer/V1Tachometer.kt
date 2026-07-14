package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer

import io.github.oni0nfr1.dynamicrider.client.graphics.amination.LoopTimer
import io.github.oni0nfr1.dynamicrider.client.graphics.util.NumberAtlas
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge.LinearExtrapolator
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.V1KartState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import kotlin.math.abs

class V1Tachometer(
    spec: Spec,
    context: HudSceneContext<V1KartState>,
    parent: ElementHolder,
) : HudElementImpl<V1KartState>(spec.layout, context, parent),
    GaugeBar by LinearExtrapolator(context.kartState)
{

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun img(name: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(
            "dynrider",
            "textures/element/v1_tachometer/$name.png"
        )
        @Suppress("NOTHING_TO_INLINE")
        inline fun numberAtlas(name: String): NumberAtlas = NumberAtlas(
            texture = img(name),
            digitWidth = NUMBER_WIDTH,
            digitHeight = NUMBER_HEIGHT,
        )

        const val SIZE_X = 272
        const val SIZE_Y = 156

        val BACKGROUND_OFF = img("background_off")
        val BACKGROUND_ON = img("background_on")

        const val BOOST_GAUGE_LEFT = 35
        const val BOOST_GAUGE_RIGHT = 237
        val BOOST_GAUGE = img("boost_gauge")
        val BOOST_BACKGROUND = img("boost_background")
        val TEAM_BOOST_GAUGE = img("team_boost_gauge")
        val TEAM_BOOST_BACKGROUND = img("team_boost_background")

        const val EXCEED_GAUGE_LEFT = 66
        const val EXCEED_GAUGE_RIGHT = 206
        const val EXCEED_POWERED_FULL_RIGHT = 209
        const val EXCEED_READY_THRESHOLD = 0.3f
        const val EXCEED_FULL_THRESHOLD = 0.99f
        val EXCEED_GAUGE = img("exceed_gauge")
        val EXCEED_OFF = img("exceed_off")
        val EXCEED_ON = img("exceed_on")
        val EXCEED_POWERED = img("exceed_powered")

        val ENGINE_ICON_OFF = img("engine_icon_off")
        val ENGINE_ICON_ON = img("engine_icon_on")

        val AUTO_GAUGE_ICON_OFF = img("autogauge_off")
        val AUTO_GAUGE_ICON_ON = img("autogauge_on")

        val DRAFT_ICON_OFF = img("draft_off")
        val DRAFT_ICON_ON = img("draft_on")

        const val NUMBER_WIDTH = 34
        const val NUMBER_HEIGHT = 46
        const val NUMBER_BOTTOM = 116
        val NUMBER_WHITE = numberAtlas("number_white")
        val NUMBER_BLUE = numberAtlas("number_blue")
    }


    val background: ResourceLocation
        get() = if (speed >= 160) BACKGROUND_ON else BACKGROUND_OFF
    val autoGaugeIcon: ResourceLocation
        get() = if (autoGauge) AUTO_GAUGE_ICON_ON else AUTO_GAUGE_ICON_OFF
    val draftIcon: ResourceLocation
        get() = if (draftActive) DRAFT_ICON_ON
        else if (draftCharging) (if (draftBlink.progress < 0.5f) DRAFT_ICON_ON else DRAFT_ICON_OFF)
        else DRAFT_ICON_OFF
    val numberFont: NumberAtlas
        get() = if (speed >= 160) NUMBER_BLUE else NUMBER_WHITE


    val draftBlinkSpeed = spec.draftBlinkSpeed


    val speed: Int
        get() = context.kartState.speed.toInt()
    val isBoosting: Boolean
        get() = context.kartState.isBoosting
    val exceedGauge: Float
        get() = context.kartState.exceedGauge
    val autoGauge: Boolean
        get() = with(context.kartState) {
            !isBoosting && !isDrifting && speed.toInt() >= 100
        }
    val draftActive: Boolean
        get() = context.kartState.draftActive
    val draftCharging: Boolean
        get() = context.kartState.draftCharging

    val draftBlink = LoopTimer(1000, draftBlinkSpeed, context::nanoTime)
    val exceedBlink = LoopTimer(1000, nanoTime = context::nanoTime)
    val exceedBlinkAlpha
        get() = 1.0f - abs(2.0f * exceedBlink.progress - 1)
    var prevExceedGauge = 0f
    var exceedGaugeDecreasing = false
        get() {
            if (exceedGauge > prevExceedGauge) {
                field = false
            } else if (exceedGauge < prevExceedGauge) {
                field = true
            }
            prevExceedGauge = exceedGauge
            return field
        }

    init {
        exceedBlink.start()
    }

    override val width: Int = SIZE_X
    override val height: Int = SIZE_Y

    fun GuiGraphics.fillImage(image: ResourceLocation) {
        blit(
            RenderType::guiTextured,
            image,
            0,
            0,
            0f,
            0f,
            width,
            height,
            width,
            height,
        )
    }

    fun GuiGraphics.renderNitroGauge(texture: ResourceLocation, gauge: Float) {
        val renderWidth = ((BOOST_GAUGE_RIGHT - BOOST_GAUGE_LEFT) * gauge).toInt()

        blit(
            RenderType::guiTextured,
            texture,
            BOOST_GAUGE_LEFT,
            0,
            BOOST_GAUGE_LEFT.toFloat(),
            0f,
            renderWidth,
            height,
            renderWidth,
            height,
            width,
            height,
        )
    }

    fun GuiGraphics.renderExceedGauge(texture: ResourceLocation, gauge: Float) {
        renderExceedGauge(texture, gauge, 0xFFFFFFFF.toInt())
    }

    fun GuiGraphics.renderExceedGauge(texture: ResourceLocation, gauge: Float, color: Int) {
        val renderWidth = ((EXCEED_GAUGE_RIGHT - EXCEED_GAUGE_LEFT) * gauge.coerceIn(0f, 1f)).toInt()
        if (renderWidth <= 0) return

        blit(
            RenderType::guiTextured,
            texture,
            EXCEED_GAUGE_LEFT,
            0,
            EXCEED_GAUGE_LEFT.toFloat(),
            0f,
            renderWidth,
            height,
            renderWidth,
            height,
            width,
            height,
            color,
        )
    }

    fun GuiGraphics.renderExceedPowered(gauge: Float, full: Boolean, color: Int) {
        val right = if (full) {
            EXCEED_POWERED_FULL_RIGHT
        } else {
            EXCEED_GAUGE_LEFT + ((EXCEED_GAUGE_RIGHT - EXCEED_GAUGE_LEFT) * gauge.coerceIn(0f, 1f)).toInt()
        }
        if (right <= 0) return

        blit(
            RenderType::guiTextured,
            EXCEED_POWERED,
            0,
            0,
            0f,
            0f,
            right,
            height,
            right,
            height,
            width,
            height,
            color,
        )
    }

    fun GuiGraphics.renderExceed(gauge: Float, decreasing: Boolean) {
        val clampedGauge = gauge.coerceIn(0f, 1f)
        val ready = clampedGauge >= EXCEED_READY_THRESHOLD
        val full = clampedGauge >= EXCEED_FULL_THRESHOLD
        val powered = full || (decreasing && ready)

        fillImage(if (ready) EXCEED_ON else EXCEED_OFF)

        if (powered) {
            val poweredColor =
                if (full) colorWithAlpha(exceedBlinkAlpha)
                else 0xFFFFFFFF.toInt()

            renderExceedPowered(clampedGauge, full, poweredColor)
        }

        renderExceedGauge(EXCEED_GAUGE, clampedGauge)
    }

    private fun colorWithAlpha(alpha: Float): Int {
        val alphaByte = (alpha.coerceIn(0f, 1f) * 255).toInt()
        return (alphaByte shl 24) or 0x00FFFFFF
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        updateGauge(deltaTracker.realtimeDeltaTicks)

        guiGraphics.fillImage(background)

        guiGraphics.fillImage(ENGINE_ICON_OFF)
        if (isBoosting) guiGraphics.fillImage(ENGINE_ICON_ON)

        guiGraphics.fillImage(draftIcon)
        guiGraphics.fillImage(autoGaugeIcon)

        guiGraphics.fillImage(BOOST_BACKGROUND)
        guiGraphics.renderNitroGauge(BOOST_GAUGE, nitroGauge)

        guiGraphics.renderExceed(exceedGauge, exceedGaugeDecreasing)

        if (context.kartState.teamBoostGaugeAvailable) {
            guiGraphics.fillImage(TEAM_BOOST_BACKGROUND)
            guiGraphics.renderNitroGauge(TEAM_BOOST_GAUGE, teamBoostGauge)
        }

        numberFont.drawNumber(
            guiGraphics = guiGraphics,
            number = speed,
            x = width / 2,
            y = NUMBER_BOTTOM,
            anchor = NumberAtlas.Anchor.BOTTOM_CENTER
        )
    }

    @Serializable
    @SerialName("V1_TACHOMETER")
    data class Spec(
        override val layout: HudLayoutSpec = HudLayoutSpec(),
        val draftBlinkSpeed: Double = 1.0,

    ) : HudElementSpec<V1Tachometer, V1KartState> {
        override fun requiredStateClass() = V1KartState::class.java
        override fun create(context: HudSceneContext<V1KartState>, parent: ElementHolder) =
            V1Tachometer(this, context, parent)
    }
}
