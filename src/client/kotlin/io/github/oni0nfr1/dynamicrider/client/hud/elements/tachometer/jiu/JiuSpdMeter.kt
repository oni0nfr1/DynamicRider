package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu

import io.github.oni0nfr1.dynamicrider.client.graphics.amination.OneShotTimer
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

class JiuSpdMeter(
    spec: Spec,
    kart: KartRef.Specific<SpeedEngine>,
    parent: ElementHolder
) : HudElementImpl<SpeedEngine>(spec.layout, kart, parent),
    Speedometer by SpdMeterImpl(kart) {
    companion object {
        val META by ElementRegistry.elementMeta<Meta>(
            elementId("jiu_tachometer/speedometer")
        )

        val BG_ATLAS get() = AtlasRegistry.requireSprite(META.background)

        val BG_ANIM_IMPACT get() = BG_ATLAS.cellAt(META.impactAnimationCell)
        val BG_ANIM get() = META.backgroundAnimationCells.map(BG_ATLAS::cellAt)

        val BACKGROUND_NO_LIGHT get() = BG_ATLAS.cellAt(META.noLightBackgroundCell)
        val BACKGROUND_LIGHT get() = BG_ATLAS.cellAt(META.lightBackgroundCell)

        val NUMBER_WHITE get() = AtlasRegistry.requireNumber(META.numberWhite)
        val NUMBER_CYAN get() = AtlasRegistry.requireNumber(META.numberCyan)
    }

    @Serializable
    data class Meta(
        @Serializable(with = ResourceLocationSerializer::class)
        val background: ResourceLocation,
        @Serializable(with = ResourceLocationSerializer::class)
        val numberWhite: ResourceLocation,
        @Serializable(with = ResourceLocationSerializer::class)
        val numberCyan: ResourceLocation,
        val backgroundAnimationCells: List<Atlas.CellPosition>,
        val impactAnimationCell: Atlas.CellPosition,
        val noLightBackgroundCell: Atlas.CellPosition,
        val lightBackgroundCell: Atlas.CellPosition,
        val numberPosX: Int,
        val numberPosY: Int,
        val lightSpeedThreshold: Double,
        val animationDurationMillis: Long,
    ) : ElementMetaData

    override val width: Int
        get() = BG_ATLAS.cellWidth
    override val height: Int
        get() = BG_ATLAS.cellHeight

    val bgAnimTimer = OneShotTimer(META.animationDurationMillis, spec.animationSpeed)

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        if (speed >= META.lightSpeedThreshold && !bgAnimTimer.running && bgAnimTimer.progress < 1.0) bgAnimTimer.start()
        if (speed < META.lightSpeedThreshold && (bgAnimTimer.running  || bgAnimTimer.progress >= 1.0)) bgAnimTimer.stop()
        val backgroundAnimation = BG_ANIM
        val animPhase = (bgAnimTimer.progress * backgroundAnimation.size).toInt()

        guiGraphics.fillImage(
            if (animPhase >= backgroundAnimation.size - 1) BACKGROUND_LIGHT
            else BACKGROUND_NO_LIGHT
        )

        backgroundAnimation.subList(0, animPhase).forEach { guiGraphics.fillImage(it) }

        val numberFont = if (animPhase >= backgroundAnimation.size - 1) NUMBER_CYAN else NUMBER_WHITE
        numberFont.drawNumber(
            guiGraphics = guiGraphics,
            number = speed.toInt(),
            x = META.numberPosX, y = META.numberPosY,
            anchor = NumberAtlas.Anchor.BOTTOM_RIGHT,
        )

        if (bgAnimTimer.progress >= 1.0) guiGraphics.fillImage(BG_ANIM_IMPACT)
        // TODO: 애니메이션 완료 후에 BG_ANIM_IMPACT가 옆으로 퍼지는 효과
    }

    class Builder : HudElementBuilder<Spec>() {
        var animationSpeed: Double = 1.0

        override fun build(layout: HudLayoutSpec) = Spec(
            layout,
            animationSpeed,
        )
    }

    @Serializable
    @SerialName("JIU_SPEEDOMETER")
    data class Spec(
        override val layout: HudLayoutSpec,
        val animationSpeed: Double,
    ) : HudElementSpec<JiuSpdMeter, SpeedEngine> {
        override fun requiredEngineClass() = SpeedEngine::class.java

        override fun create(
            kart: KartRef.Specific<SpeedEngine>,
            parent: ElementHolder
        ) = JiuSpdMeter(this, kart, parent)

    }
}
