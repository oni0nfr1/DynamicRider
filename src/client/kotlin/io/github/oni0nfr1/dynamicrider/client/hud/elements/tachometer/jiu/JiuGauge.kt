package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu

import io.github.oni0nfr1.dynamicrider.client.graphics.texture.Atlas
import io.github.oni0nfr1.dynamicrider.client.graphics.render.shape.drawGauge
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge.LinearExtrapolator
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudElementInfo
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudLayout
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

class JiuGauge(
    spec: Spec,
    context: HudSceneContext<NitroKartState>,
    parent: ElementHolder,
) : HudElementImpl<NitroKartState>(spec.layout, context, parent),
    GaugeBar by LinearExtrapolator(context.kartState)
{
    companion object {
        val META by ElementRegistry.elementMeta<Meta>(
            elementId("jiu_tachometer/gauge")
        )

        val GAUGE get() = AtlasRegistry.requireSprite(META.gaugeAtlas)
        val ICON get() = AtlasRegistry.requireSprite(META.iconAtlas)

        val GAUGE_BG get() = GAUGE.cellAt(META.backgroundCell)
        val NITRO_GAUGE get() = GAUGE.cellAt(META.nitroGaugeCell)
        val TEAM_GAUGE get() = GAUGE.cellAt(META.teamGaugeCell)

        val ICON_ON get() = ICON.cellAt(META.iconOnCell)
        val ICON_OFF get() = ICON.cellAt(META.iconOffCell)
    }

    @Serializable
    data class Meta(
        @Serializable(with = ResourceLocationSerializer::class)
        val gaugeAtlas: ResourceLocation,
        @Serializable(with = ResourceLocationSerializer::class)
        val iconAtlas: ResourceLocation,
        val backgroundCell: Atlas.CellPosition,
        val nitroGaugeCell: Atlas.CellPosition,
        val teamGaugeCell: Atlas.CellPosition,
        val iconOnCell: Atlas.CellPosition,
        val iconOffCell: Atlas.CellPosition,
        val gaugeX: Int,
        val gaugeY: Int,
        val iconX: Int,
        val iconY: Int,
        val fillRegion: GaugeFillRegion,
        val elementWidth: Int,
        val elementHeight: Int,
    ) : ElementMetaData

    override val width: Int get() = META.elementWidth
    override val height: Int get() = META.elementHeight

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        updateGauge(deltaTracker.realtimeDeltaTicks)

        GAUGE_BG.draw(guiGraphics, META.gaugeX, META.gaugeY)
        ICON_OFF.draw(guiGraphics, META.iconX, META.iconY)

        NITRO_GAUGE.drawGauge(guiGraphics, META.fillRegion, nitroGauge, META.gaugeX, META.gaugeY)
        TEAM_GAUGE.drawGauge(guiGraphics, META.fillRegion, teamBoostGauge, META.gaugeX, META.gaugeY)

        if (nitroGauge == 1f) ICON_ON.draw(guiGraphics, META.iconX, META.iconY)
    }

    @Serializable
    @SerialName("JIU_GAUGE")
    @HudElementInfo(category = "gauge")
    data class Spec(
        @HudLayout
        override val layout: HudLayoutSpec = HudLayoutSpec(),

    ) : HudElementSpec<JiuGauge, NitroKartState> {
        override fun requiredStateClass() = NitroKartState::class.java

        override fun create(
            context: HudSceneContext<NitroKartState>,
            parent: ElementHolder
        ) = JiuGauge(this, context, parent)

    }
}
