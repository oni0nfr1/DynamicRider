package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge.LinearExtrapolator
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.graphics.util.Atlas
import io.github.oni0nfr1.dynamicrider.client.graphics.util.drawGauge
import io.github.oni0nfr1.dynamicrider.client.graphics.util.fillImage
import io.github.oni0nfr1.dynamicrider.client.resource.ResourceLocationSerializer
import io.github.oni0nfr1.dynamicrider.client.resource.atlas.AtlasRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementMetaData
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.element.data.GaugeFillRegion
import io.github.oni0nfr1.dynamicrider.client.resource.elementId
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class ChargeGauge(
    spec: Spec,
    context: HudSceneContext<NitroKartState>,
    parent: ElementHolder,
) : HudElementImpl<NitroKartState>(spec.layout, context, parent),
    GaugeBar by LinearExtrapolator(context.kartState) {

    companion object {
        val META by ElementRegistry.elementMeta<Meta>(
            elementId("charge_tachometer/gauge")
        )

        val ATLAS
            get() = AtlasRegistry.requireSprite(META.atlas)
        val BACKGROUND
            get() = ATLAS.cellAt(META.backgroundCell)
        val NITRO_GAUGE
            get() = ATLAS.cellAt(META.nitroGaugeCell)
        val TEAM_GAUGE
            get() = ATLAS.cellAt(META.teamGaugeCell)
    }

    @Serializable
    data class Meta(
        @Serializable(with = ResourceLocationSerializer::class)
        val atlas: ResourceLocation,
        val backgroundCell: Atlas.CellPosition,
        val nitroGaugeCell: Atlas.CellPosition,
        val teamGaugeCell: Atlas.CellPosition,
        val nitroFillRegion: GaugeFillRegion,
        val teamFillRegion: GaugeFillRegion,
    ) : ElementMetaData

    override val width: Int
        get() = ATLAS.cellWidth
    override val height: Int
        get() = ATLAS.cellHeight

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        updateGauge(deltaTracker.realtimeDeltaTicks)
        guiGraphics.fillImage(BACKGROUND)
        NITRO_GAUGE.drawGauge(guiGraphics, META.nitroFillRegion, nitroGauge)
        TEAM_GAUGE.drawGauge(guiGraphics, META.teamFillRegion, teamBoostGauge)
    }

    @Serializable
    @SerialName("CHARGE_GAUGE")
    data class Spec(
        override val layout: HudLayoutSpec,
    ) : HudElementSpec<ChargeGauge, NitroKartState> {
        override fun requiredStateClass() = NitroKartState::class.java

        override fun create(
            context: HudSceneContext<NitroKartState>,
            parent: ElementHolder,
        ) = ChargeGauge(this, context, parent)
    }
}
