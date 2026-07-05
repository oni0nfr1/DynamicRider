package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge

import io.github.oni0nfr1.dynamicrider.client.graphics.amination.LoopTimer
import io.github.oni0nfr1.dynamicrider.client.graphics.util.Atlas
import io.github.oni0nfr1.dynamicrider.client.graphics.util.NumberAtlas
import io.github.oni0nfr1.dynamicrider.client.graphics.util.fillImage
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedometer.Speedometer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedometer.impl.SpdMeterImpl
import io.github.oni0nfr1.dynamicrider.client.resource.ResourceLocationSerializer
import io.github.oni0nfr1.dynamicrider.client.resource.atlas.AtlasRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementMetaData
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.elementId
import io.github.oni0nfr1.skid.client.api.engine.SpeedEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class ChargeSpdMeter(
    spec: Spec,
    kart: KartRef.Specific<SpeedEngine>,
    parent: ElementHolder
) : HudElementImpl<SpeedEngine>(spec.layout, kart, parent),
    Speedometer by SpdMeterImpl(kart)
{
    companion object {
        val META by ElementRegistry.elementMeta<Meta>(
            elementId("charge_tachometer/speedometer")
        )

        val BG_ATLAS
            get() = AtlasRegistry.requireSprite(META.background)

        val BG_ANIM_EMPTY get() = BG_ATLAS.cellAt(META.emptyBackgroundAnimationCell)
        val BG_ANIM get() = buildList {
            META.backgroundAnimationCell.forEach { add(BG_ATLAS.cellAt(it)) }
        }

        val BACKGROUND_NO_LIGHT get() = BG_ATLAS.cellAt(META.noLightBackgroundCell)
        val BACKGROUND_LIGHT get() = BG_ATLAS.cellAt(META.lightBackgroundCell)

        val NUMBER_WHITE get() = AtlasRegistry.requireNumber(META.numberWhite)
        val NUMBER_BLUE get() = AtlasRegistry.requireNumber(META.numberBlue)
        val NUMBER_RED get() = AtlasRegistry.requireNumber(META.numberRed)

    }

    @Serializable
    data class Meta(
        @Serializable(with = ResourceLocationSerializer::class)
        val background: ResourceLocation,
        @Serializable(with = ResourceLocationSerializer::class)
        val numberWhite: ResourceLocation,
        @Serializable(with = ResourceLocationSerializer::class)
        val numberBlue: ResourceLocation,
        @Serializable(with = ResourceLocationSerializer::class)
        val numberRed: ResourceLocation,

        val backgroundAnimationCell: List<Atlas.CellPosition>,
        val emptyBackgroundAnimationCell: Atlas.CellPosition,
        val noLightBackgroundCell: Atlas.CellPosition,
        val lightBackgroundCell: Atlas.CellPosition,

        val numberPosX: Int,
        val numberPosY: Int,

        val lightSpeedThreshold: Double,
        val redNumberSpeedThreshold: Double,
        val animationSpeedBase: Double,

        val backgroundWidth: Int,
        val backgroundHeight: Int,
    ) : ElementMetaData

    override val width: Int
        get() = META.backgroundWidth
    override val height: Int
        get() = META.backgroundHeight

    private val animationTimer = LoopTimer(1000, 0.0)
    private val animationSpeed = spec.animationSpeed

    init {
        animationTimer.start()
    }

    private val background: Atlas.Cell
        get() = if (speed > META.lightSpeedThreshold) BACKGROUND_LIGHT else BACKGROUND_NO_LIGHT

    private val numberFont: NumberAtlas
        get() = when {
            speed > META.redNumberSpeedThreshold -> NUMBER_RED
            speed > META.lightSpeedThreshold -> NUMBER_BLUE
            else -> NUMBER_WHITE
        }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        guiGraphics.fillImage(background)

        numberFont.drawNumber(
            guiGraphics = guiGraphics,
            number = speed.toInt().coerceAtLeast(0),
            x = META.numberPosX,
            y = META.numberPosY,
            anchor = NumberAtlas.Anchor.BOTTOM_RIGHT,
        )

        animationTimer.speed = animationSpeed * speed * META.animationSpeedBase
        guiGraphics.fillImage(BG_ANIM_EMPTY)

        if (speed > 0.0) {
            val frames = BG_ANIM
            if (frames.isNotEmpty()) {
                val frameIndex = (animationTimer.progress * frames.size)
                    .toInt()
                    .coerceIn(0, frames.lastIndex)
                guiGraphics.fillImage(frames[frameIndex])
            }
        }
    }

    class Builder : HudElementBuilder<Spec>() {
        var animationSpeed: Double = 1.0

        override fun build(layout: HudLayoutSpec) = Spec(
            layout = layout,
            animationSpeed = animationSpeed.coerceAtLeast(0.0),
        )
    }

    @Serializable
    @SerialName("CHARGE_SPEEDOMETER")
    data class Spec(
        override val layout: HudLayoutSpec,
        val animationSpeed: Double,
    ) : HudElementSpec<ChargeSpdMeter, SpeedEngine> {
        override fun requiredEngineClass() = SpeedEngine::class.java

        override fun create(
            kart: KartRef.Specific<SpeedEngine>,
            parent: ElementHolder
        ) = ChargeSpdMeter(this, kart, parent)

    }

}
