package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge

import io.github.oni0nfr1.dynamicrider.client.graphics.amination.LoopTimer
import io.github.oni0nfr1.dynamicrider.client.graphics.util.Atlas
import io.github.oni0nfr1.dynamicrider.client.graphics.util.fillImage
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.resource.ResourceLocationSerializer
import io.github.oni0nfr1.dynamicrider.client.resource.atlas.AtlasRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementMetaData
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.elementId
import io.github.oni0nfr1.dynamicrider.client.hud.state.ChargeKartState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class ChargeIcons(
    spec: Spec,
    context: HudSceneContext<ChargeKartState>,
    parent: ElementHolder,
) : HudElementImpl<ChargeKartState>(spec.layout, context, parent) {

    companion object {
        val META by ElementRegistry.elementMeta<Meta>(
            elementId("charge_tachometer/icons")
        )

        val ATLAS
            get() = AtlasRegistry.requireSprite(META.atlas)
        val BACKGROUND
            get() = ATLAS.cellAt(META.backgroundCell)
        val AUTO_GAUGE
            get() = ATLAS.cellAt(META.autoGaugeCell)
        val DRAFT
            get() = ATLAS.cellAt(META.draftCell)
    }

    @Serializable
    data class Meta(
        @Serializable(with = ResourceLocationSerializer::class)
        val atlas: ResourceLocation,
        val backgroundCell: Atlas.CellPosition,
        val autoGaugeCell: Atlas.CellPosition,
        val draftCell: Atlas.CellPosition,
        val autoGaugeSpeedThreshold: Double,
    ) : ElementMetaData

    val autoGauge: Boolean
        get() = with(context.kartState) {
            !isBoosting && !isDrifting && speed >= META.autoGaugeSpeedThreshold
        }

    val draftActive: Boolean
        get() = context.kartState.draftActive

    val draftCharging: Boolean
        get() = context.kartState.draftCharging

    val draftBlink = LoopTimer(1000, spec.draftBlinkSpeed, context::nanoTime)

    override val width: Int
        get() = ATLAS.cellWidth
    override val height: Int
        get() = ATLAS.cellHeight

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        if (draftCharging && !draftBlink.running) draftBlink.start()
        if (!draftCharging && !draftActive && draftBlink.running) draftBlink.stop()

        guiGraphics.fillImage(BACKGROUND)
        if (autoGauge) guiGraphics.fillImage(AUTO_GAUGE)
        if (draftActive || (draftCharging && draftBlink.progress < 0.5f)) {
            guiGraphics.fillImage(DRAFT)
        }
    }

    @Serializable
    @SerialName("CHARGE_ICONS")
    data class Spec(
        override val layout: HudLayoutSpec = HudLayoutSpec(),
        val draftBlinkSpeed: Double = 1.0,
    ) : HudElementSpec<ChargeIcons, ChargeKartState> {
        override fun requiredStateClass() = ChargeKartState::class.java

        override fun create(
            context: HudSceneContext<ChargeKartState>,
            parent: ElementHolder,
        ) = ChargeIcons(this, context, parent)
    }
}
