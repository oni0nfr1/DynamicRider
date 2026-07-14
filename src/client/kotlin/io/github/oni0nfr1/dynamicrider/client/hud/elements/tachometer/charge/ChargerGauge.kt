package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
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
import io.github.oni0nfr1.dynamicrider.client.hud.state.ChargeKartState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class ChargerGauge(
    spec: Spec,
    context: HudSceneContext<ChargeKartState>,
    parent: ElementHolder,
) : HudElementImpl<ChargeKartState>(spec.layout, context, parent) {

    companion object {
        val META by ElementRegistry.elementMeta<Meta>(
            elementId("charge_tachometer/charger_gauge")
        )

        val ATLAS
            get() = AtlasRegistry.requireSprite(META.atlas)
        val GAUGE
            get() = ATLAS.cellAt(META.gaugeCell)
        val ICON_OFF
            get() = ATLAS.cellAt(META.iconOffCell)
        val ICON_ON
            get() = ATLAS.cellAt(META.iconOnCell)
    }

    @Serializable
    data class Meta(
        @Serializable(with = ResourceLocationSerializer::class)
        val atlas: ResourceLocation,
        val gaugeCell: Atlas.CellPosition,
        val iconOffCell: Atlas.CellPosition,
        val iconOnCell: Atlas.CellPosition,
        val fillRegion: GaugeFillRegion,
        val maxEpsilon: Float,
    ) : ElementMetaData

    val chargerGauge: Float
        get() = context.kartState.chargerGauge

    override val width: Int
        get() = ATLAS.cellWidth
    override val height: Int
        get() = ATLAS.cellHeight

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        val gauge = chargerGauge.coerceIn(0f, 1f)
        guiGraphics.fillImage(
            if (gauge >= 1f - META.maxEpsilon) ICON_ON else ICON_OFF
        )
        GAUGE.drawGauge(guiGraphics, META.fillRegion, gauge)
    }

    @Serializable
    @SerialName("CHARGER_GAUGE")
    data class Spec(
        override val layout: HudLayoutSpec = HudLayoutSpec(),
    ) : HudElementSpec<ChargerGauge, ChargeKartState> {
        override fun requiredStateClass() = ChargeKartState::class.java

        override fun create(
            context: HudSceneContext<ChargeKartState>,
            parent: ElementHolder,
        ) = ChargerGauge(this, context, parent)
    }
}
