package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer

import io.github.oni0nfr1.dynamicrider.client.graphics.amination.LoopTimer
import io.github.oni0nfr1.dynamicrider.client.graphics.util.NumberAtlas
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.rider.backend.bossbar.KartTeamBoostTracker
import io.github.oni0nfr1.skid.client.api.engine.ChargeEngine
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

class ChargeTachometer(
    spec: Spec,
    kart: KartRef.Specific<ChargeEngine>
) : HudElementImpl<ChargeEngine>(spec.layout, kart) {

    // assets
    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun img(name: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(
            "dynrider",
            "textures/element/charge_tachometer/$name.png"
        )
        @Suppress("NOTHING_TO_INLINE")
        inline fun numberAtlas(name: String): NumberAtlas = NumberAtlas(
            texture = img(name),
            digitWidth = NUMBER_WIDTH,
            digitHeight = NUMBER_HEIGHT,
            digitOrder = "1234567890",
        )

        const val SIZE_X = 376
        const val SIZE_Y = 117

        val BACKGROUND_NO_LIGHT = img("tachometer_nolight")
        val BACKGROUND_LIGHT = img("tachometer_light")

        val ENGINE_ICON_ON = img("engine_icon_on")
        val ENGINE_ICON_OFF = img("engine_icon_off")

        const val BOOST_GAUGE_LEFT = 80
        const val BOOST_GAUGE_RIGHT = 295
        val BOOST_GAUGE = img("boost")
        val TEAM_BOOST_GAUGE = img("team_boost")
        val BOOST_GAUGE_EMPTY = img("boost_empty")

        const val CHARGER_MAX_EPSILON = 0.05
        const val CHARGER_GAUGE_TOP = 27
        val CHARGER_ICON_ON = img("charger_on")
        val CHARGER_ICON_OFF = img("charger_off")
        val CHARGER_GAUGE = img("charger_gauge")

        const val MOVE_ANIM_SPEED_BASE = 0.008
        val MOVE_ANIM_EMPTY = img("move_anim_empty")
        val MOVE_ANIM_BOTTOM = img("move_anim_bottom")
        val MOVE_ANIM_MIDDLE = img("move_anim_middle")
        val MOVE_ANIM_TOP = img("move_anim_top")

        const val NUMBER_POS_X = 240
        const val NUMBER_POS_Y = 93

        const val NUMBER_WIDTH = 46
        const val NUMBER_HEIGHT = 58
        val NUMBER_WHITE = numberAtlas("number_white")
        val NUMBER_BLUE = numberAtlas("number_blue")
        val NUMBER_RED = numberAtlas("number_red")

        val AUTO_GAUGE_ICON_ON = img("autogauge_on")
        val AUTO_GAUGE_ICON_OFF = img("autogauge_off")

        val DRAFT_ICON_ON = img("draft_on")
        val DRAFT_ICON_OFF = img("draft_off")
    }

    val background: ResourceLocation
        get() = if (speed > 120) BACKGROUND_LIGHT else BACKGROUND_NO_LIGHT
    val engineIcon: ResourceLocation
        get() = if (speed > 120) ENGINE_ICON_ON else ENGINE_ICON_OFF
    val numberFont: NumberAtlas
        get() = if (speed > 300)      NUMBER_RED
                else if (speed > 120) NUMBER_BLUE
                else                  NUMBER_WHITE
    val chargerIcon: ResourceLocation
        get() = if (chargerGauge >= 1f - CHARGER_MAX_EPSILON) CHARGER_ICON_ON else CHARGER_ICON_OFF
    val autoGaugeIcon: ResourceLocation
        get() = if (autoGauge) AUTO_GAUGE_ICON_ON else AUTO_GAUGE_ICON_OFF
    val draftIcon: ResourceLocation
        get() = if (draftActive) DRAFT_ICON_ON
                else if (draftCharging) (if (draftBlink.progress < 0.5f) DRAFT_ICON_ON else DRAFT_ICON_OFF)
                else DRAFT_ICON_OFF

    // element properties
    val animationSpeed = spec.animationSpeed
    val draftBlinkSpeed = spec.draftBlinkSpeed

    // runtime states
    val speed: Int
        get() = kart.accessEngine { engine ->
            engine.tachometer?.speed?.toInt()
        } ?: 0
    val chargerGauge: Float
        get() = kart.accessEngine { engine ->
            engine.tachometer?.chargerGauge
        } ?: 0f
    val nitroGauge: Float
        get() = kart.accessEngine { engine ->
            engine.tachometer?.gauge?.toFloat()
        } ?: 0f
    val autoGauge: Boolean
        get() = kart.accessEngine { engine ->
            val speed = engine.tachometer?.speed ?: return@accessEngine null
            val isDrifting = engine.isDrifting
            val isBoosting = engine.isBoosting

            return !isBoosting && !isDrifting && speed.toInt() >= 100
        } ?: false
    val draftActive: Boolean
        get() = kart.accessEngine { engine ->
            engine.draftActive
        } ?: false
    val draftCharging: Boolean
        get() = kart.accessEngine { engine ->
            engine.draftCharging
        } ?: false
    val teamBoostGauge: Float
        get() = if (KartTeamBoostTracker.gaugeExists) KartTeamBoostTracker.gauge else 0f
    val animationTimer = LoopTimer(1000, 0.0)
    val draftBlink = LoopTimer(1000, draftBlinkSpeed)

    init {
        animationTimer.start()
        draftBlink.start()
    }

    override fun resolveSize() {
        setSize(SIZE_X, SIZE_Y)
    }

    fun GuiGraphics.fillImage(image: ResourceLocation) {
        blit(
            RenderType::guiTextured,
            image,
            0,
            0,
            0f,
            0f,
            size.x,
            size.y,
            size.x,
            size.y,
        )
    }

    fun GuiGraphics.renderChargerGauge(gauge: Float) {
        val top = (SIZE_Y - (SIZE_Y - CHARGER_GAUGE_TOP) * gauge).toInt()
        val renderHeight = size.y - top

        if (renderHeight > 0) {
            blit(
                RenderType::guiTextured,
                CHARGER_GAUGE,
                0,
                top,
                0f,
                top.toFloat(),
                size.x,
                size.y - top,
                size.x,
                size.y - top,
                size.x,
                size.y,
            )
        }
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
            size.y,
            renderWidth,
            size.y,
            size.x,
            size.y,
        )
    }

    fun GuiGraphics.renderAnimation() {
        val speed = this@ChargeTachometer.speed
        animationTimer.speed = animationSpeed * speed.toDouble() * MOVE_ANIM_SPEED_BASE

        fillImage(MOVE_ANIM_EMPTY)
        if (speed == 0) return

        val prog = animationTimer.progress
        if      (prog >= 2 / 3f) fillImage(MOVE_ANIM_TOP)
        else if (prog >= 1 / 3f) fillImage(MOVE_ANIM_MIDDLE)
        else                     fillImage(MOVE_ANIM_BOTTOM)
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        guiGraphics.fillImage(background)
        guiGraphics.fillImage(engineIcon)
        guiGraphics.fillImage(chargerIcon)
        guiGraphics.fillImage(BOOST_GAUGE_EMPTY)

        guiGraphics.renderChargerGauge(chargerGauge)
        guiGraphics.renderNitroGauge(BOOST_GAUGE, nitroGauge)
        guiGraphics.renderNitroGauge(TEAM_BOOST_GAUGE, teamBoostGauge)

        guiGraphics.fillImage(autoGaugeIcon)
        guiGraphics.fillImage(draftIcon)

        numberFont.drawNumber(
            guiGraphics = guiGraphics,
            number = speed,
            x = NUMBER_POS_X,
            y = NUMBER_POS_Y,
            anchor = NumberAtlas.Anchor.BOTTOM_RIGHT,
        )

        guiGraphics.renderAnimation()
    }

    class Builder : HudElementBuilder<Spec>() {
        var animationSpeed = 1.0
        var draftBlinkSpeed = 1.0

        override fun build(layout: HudLayoutSpec) =
            Spec(
                layout = layout,
                animationSpeed = animationSpeed.coerceAtLeast(0.0),
                draftBlinkSpeed = draftBlinkSpeed.coerceAtLeast(0.0),
            )
    }

    @Serializable
    @SerialName("CHARGE_TACHOMETER")
    data class Spec(
        override val layout: HudLayoutSpec,
        val animationSpeed: Double,
        val draftBlinkSpeed: Double,
    ) : HudElementSpec<ChargeTachometer, ChargeEngine>() {
        override fun requiredEngineClass(): Class<out KartEngine> = ChargeEngine::class.java

        override fun create(kart: KartRef.Specific<ChargeEngine>) = ChargeTachometer(this, kart)
    }

}
