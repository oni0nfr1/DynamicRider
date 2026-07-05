package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.charge

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.dsl.HudElementBuilder
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
import io.github.oni0nfr1.skid.client.api.engine.ChargeEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class ChargerGauge(
    spec: Spec,
    kart: KartRef.Specific<ChargeEngine>,
    parent: ElementHolder,
) : HudElementImpl<ChargeEngine>(spec.layout, kart, parent) {

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
        get() = kart.accessEngine { it.tachometer?.chargerGauge } ?: 0f

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

    class Builder : HudElementBuilder<Spec>() {
        override fun build(layout: HudLayoutSpec) = Spec(layout)
    }

    @Serializable
    @SerialName("CHARGER_GAUGE")
    data class Spec(
        override val layout: HudLayoutSpec,
    ) : HudElementSpec<ChargerGauge, ChargeEngine> {
        override fun requiredEngineClass() = ChargeEngine::class.java

        override fun create(
            kart: KartRef.Specific<ChargeEngine>,
            parent: ElementHolder,
        ) = ChargerGauge(this, kart, parent)
    }
}
