package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.x

import io.github.oni0nfr1.dynamicrider.client.animation.LoopTimer
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.Atlas
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.NumberAtlas
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.SpriteAtlas
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.fillImage
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge.LinearExtrapolator
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedometer.Speedometer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedometer.impl.SpdMeterImpl
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudElementInfo
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudLayout
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import io.github.oni0nfr1.dynamicrider.client.resource.ResourceLocationSerializer
import io.github.oni0nfr1.dynamicrider.client.resource.atlas.AtlasRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementMetaData
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.elementId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import org.joml.Vector2f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class XSpdMeter(
    private val spec: Spec,
    context: HudSceneContext<NitroKartState>,
    parent: ElementHolder,
) : HudElementImpl<NitroKartState>(spec.layout, context, parent),
    Speedometer by SpdMeterImpl(context.kartState),
    GaugeBar by LinearExtrapolator(context.kartState) {

    companion object {
        val META by ElementRegistry.elementMeta<Meta>(
            elementId("x_tachometer/speedometer"),
        )

        val BACKGROUND_ATLAS get() = AtlasRegistry.requireSprite(META.background)
        val GAUGE_ATLAS get() = AtlasRegistry.requireSprite(META.gauge)

        val BACKGROUND_ANIMATION
            get() = META.backgroundAnimationCells.map(BACKGROUND_ATLAS::cellAt)
        val BACKGROUND_NO_LIGHT get() = BACKGROUND_ATLAS.cellAt(META.noLightBackgroundCell)
        val BACKGROUND_LIGHT get() = BACKGROUND_ATLAS.cellAt(META.lightBackgroundCell)

        val TEAM_GAUGE_BACKGROUND get() = GAUGE_ATLAS.cellAt(META.teamGaugeBackgroundCell)
        val NITRO_GAUGE get() = GAUGE_ATLAS.cellAt(META.nitroGaugeCell)
        val TEAM_GAUGE get() = GAUGE_ATLAS.cellAt(META.teamGaugeCell)

        val NUMBER_WHITE get() = AtlasRegistry.requireNumber(META.numberWhite)
        val NUMBER_CYAN get() = AtlasRegistry.requireNumber(META.numberCyan)
    }

    @Serializable
    data class Meta(
        @Serializable(with = ResourceLocationSerializer::class)
        val background: ResourceLocation,
        @Serializable(with = ResourceLocationSerializer::class)
        val gauge: ResourceLocation,
        @Serializable(with = ResourceLocationSerializer::class)
        val numberWhite: ResourceLocation,
        @Serializable(with = ResourceLocationSerializer::class)
        val numberCyan: ResourceLocation,
        val backgroundAnimationCells: List<Atlas.CellPosition>,
        val noLightBackgroundCell: Atlas.CellPosition,
        val lightBackgroundCell: Atlas.CellPosition,
        val teamGaugeBackgroundCell: Atlas.CellPosition,
        val nitroGaugeCell: Atlas.CellPosition,
        val teamGaugeCell: Atlas.CellPosition,
        val gaugeCenterX: Float,
        val gaugeCenterY: Float,
        val gaugeStartAngleDegrees: Float,
        val gaugeSweepDegrees: Float,
        val numberPosX: Int,
        val numberPosY: Int,
        val lightSpeedThreshold: Double,
        val animationSpeedBase: Double,
    ) : ElementMetaData

    override val width: Int
        get() = BACKGROUND_ATLAS.cellWidth
    override val height: Int
        get() = BACKGROUND_ATLAS.cellHeight

    private val animationTimer = LoopTimer(1000, 0.0, context::nanoTime).also(LoopTimer::start)
    private val gaugeStartDirection = Vector2f()
    private val gaugeEndDirection = Vector2f()

    private val lit: Boolean
        get() = speed > META.lightSpeedThreshold

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker,
    ) {
        updateGauge(deltaTracker.realtimeDeltaTicks)

        guiGraphics.fillImage(if (lit) BACKGROUND_LIGHT else BACKGROUND_NO_LIGHT)
        drawBackgroundAnimation(guiGraphics)

        if (context.kartState.teamBoostGaugeAvailable) {
            TEAM_GAUGE_BACKGROUND.draw(guiGraphics, 0, 0)
            TEAM_GAUGE.drawGaugeArc(guiGraphics, teamBoostGauge)
        }
        NITRO_GAUGE.drawGaugeArc(guiGraphics, nitroGauge)

        (if (lit) NUMBER_CYAN else NUMBER_WHITE).drawNumber(
            guiGraphics = guiGraphics,
            number = speed.toInt().coerceAtLeast(0),
            x = META.numberPosX,
            y = META.numberPosY,
            anchor = NumberAtlas.Anchor.BOTTOM_CENTER,
        )
    }

    private fun drawBackgroundAnimation(guiGraphics: GuiGraphics) {
        animationTimer.speed = spec.animationSpeed * speed.coerceAtLeast(0.0) * META.animationSpeedBase
        if (speed <= 0.0) return

        val frames = BACKGROUND_ANIMATION
        if (frames.isEmpty()) return
        val frameIndex = (animationTimer.progress * frames.size)
            .toInt()
            .coerceIn(0, frames.lastIndex)
        guiGraphics.fillImage(frames[frameIndex])
    }

    private fun SpriteAtlas.Cell.drawGaugeArc(
        guiGraphics: GuiGraphics,
        value: Float,
    ) {
        val progress = value.coerceIn(0f, 1f)
        val startAngle = META.gaugeStartAngleDegrees / 180.0 * PI
        val sweepDegrees = META.gaugeSweepDegrees * progress
        val endAngle = (META.gaugeStartAngleDegrees + sweepDegrees) / 180.0 * PI
        gaugeStartDirection.set(
            cos(startAngle).toFloat(),
            sin(startAngle).toFloat(),
        )
        gaugeEndDirection.set(cos(endAngle).toFloat(), sin(endAngle).toFloat())
        drawArcClipped(
            guiGraphics = guiGraphics,
            x = 0,
            y = 0,
            centerX = META.gaugeCenterX,
            centerY = META.gaugeCenterY,
            startDirection = gaugeStartDirection,
            endDirection = gaugeEndDirection,
            fillAmount = progress,
            majorArc = sweepDegrees > 180f,
        )
    }

    @Serializable
    @SerialName("X_SPEEDOMETER")
    @HudElementInfo(category = "speedometer")
    data class Spec(
        @HudLayout
        override val layout: HudLayoutSpec = HudLayoutSpec(),
        val animationSpeed: Double = 1.0,
    ) : HudElementSpec<XSpdMeter, NitroKartState> {
        override fun requiredStateClass() = NitroKartState::class.java

        override fun create(
            context: HudSceneContext<NitroKartState>,
            parent: ElementHolder,
        ) = XSpdMeter(this, context, parent)
    }
}
