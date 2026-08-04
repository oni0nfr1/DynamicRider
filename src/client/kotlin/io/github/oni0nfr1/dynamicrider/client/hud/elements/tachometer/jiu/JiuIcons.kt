package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu

import io.github.oni0nfr1.dynamicrider.client.animation.LoopTimer
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.Atlas
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.fillImage
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudElementInfo
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudLayout
import io.github.oni0nfr1.dynamicrider.client.resource.ResourceLocationSerializer
import io.github.oni0nfr1.dynamicrider.client.resource.atlas.AtlasRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementMetaData
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.elementId
import io.github.oni0nfr1.dynamicrider.client.hud.state.JiuKartState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class JiuIcons(
    spec: Spec,
    context: HudSceneContext<JiuKartState>,
    parent: ElementHolder,
) : HudElementImpl<JiuKartState>(spec.layout, context, parent) {

    companion object {
        val META by ElementRegistry.elementMeta<Meta>(
            elementId("jiu_tachometer/icons")
        )

        val ATLAS get() = AtlasRegistry.requireSprite(META.atlas)

        val BACKGROUND get() = ATLAS.cellAt(META.backgroundCell)
        val DRAFT get() = ATLAS.cellAt(META.draftCell)
        val AUTO_GAUGE get() = ATLAS.cellAt(META.autoGaugeCell)
    }

    @Serializable
    data class Meta(
        @Serializable(with = ResourceLocationSerializer::class)
        val atlas: ResourceLocation,
        val backgroundCell: Atlas.CellPosition,
        val draftCell: Atlas.CellPosition,
        val autoGaugeCell: Atlas.CellPosition,
        val autoGaugeSpeedThreshold: Double,
    ) : ElementMetaData

    override val width: Int
        get() = ATLAS.cellWidth
    override val height: Int
        get() = ATLAS.cellHeight

    val draftActive: Boolean
        get() = context.kartState.draftActive
    val draftCharging: Boolean
        get() = context.kartState.draftCharging

    val autoGauge: Boolean
        get() = with(context.kartState) {
            !isBoosting && !isDrifting && speed >= META.autoGaugeSpeedThreshold
        }

    val draftBlink = LoopTimer(1000, spec.draftBlinkSpeed, context::nanoTime)

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        val draftBlinking = draftCharging && !draftActive
        if (draftBlinking && !draftBlink.running) draftBlink.start()
        if (!draftBlinking && draftBlink.running) draftBlink.stop()

        guiGraphics.fillImage(BACKGROUND)

        if (draftActive) guiGraphics.fillImage(DRAFT)
        else if (draftCharging && draftBlink.progress < 0.5f) guiGraphics.fillImage(DRAFT)

        if (autoGauge) guiGraphics.fillImage(AUTO_GAUGE)
    }

    @Serializable
    @SerialName("JIU_ICONS")
    @HudElementInfo(category = "tachometer")
    data class Spec(
        @HudLayout
        override val layout: HudLayoutSpec = HudLayoutSpec(),
        val draftBlinkSpeed: Double = 1.0,
    ) : HudElementSpec<JiuIcons, JiuKartState> {
        override fun requiredStateClass() = JiuKartState::class.java

        override fun create(
            context: HudSceneContext<JiuKartState>,
            parent: ElementHolder
        ) = JiuIcons(this, context, parent)
    }
}
