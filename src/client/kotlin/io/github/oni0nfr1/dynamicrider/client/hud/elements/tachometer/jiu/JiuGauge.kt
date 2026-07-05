package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.graphics.util.Atlas
import io.github.oni0nfr1.dynamicrider.client.graphics.util.fillImage
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.bridge.LinearExtrapolator
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.resource.atlas.AtlasRegistry
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class JiuGauge(
    spec: Spec,
    kart: KartRef.Specific<NitroEngine>,
    parent: ElementHolder,
) : HudElementImpl<NitroEngine>(spec.layout, kart, parent),
    GaugeBar by LinearExtrapolator(kart)
{
    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun atlasId(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(
            ResourceStore.MOD_ID,
            "element/jiu_tachometer/$path"
        )

        val GAUGE by AtlasRegistry.sprite(atlasId("gauge"))
        val ICON by AtlasRegistry.sprite(atlasId("gauge_icon"))

        const val GAUGE_X = 47
        const val GAUGE_Y = 3
        val GAUGE_BG    get() = GAUGE.cellAt(0, 0)
        val NITRO_GAUGE get() = GAUGE.cellAt(1, 0)
        val TEAM_GAUGE  get() = GAUGE.cellAt(2, 0)

        const val ICON_X = 0
        const val ICON_Y = 0
        val ICON_ON  get() = ICON.cellAt(0, 0)
        val ICON_OFF get() = ICON.cellAt(1, 0)

        const val GAUGE_LEFT = 1
        const val GAUGE_RIGHT = 209
    }

    override val width: Int
        get() = GAUGE_X * 2 + GAUGE.cellWidth
    override val height: Int = 38

    private fun GuiGraphics.drawGauge(img: Atlas.Cell, value: Float) {
        img.drawRegion(
            guiGraphics = this,
            x = GAUGE_LEFT + GAUGE_X,
            y = GAUGE_Y,
            sourceX = GAUGE_LEFT,
            sourceY = 0,
            sourceWidth = (value * (GAUGE_RIGHT - GAUGE_LEFT)).toInt() + GAUGE_LEFT,
            sourceHeight = img.height,
        )
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        updateGauge(deltaTracker.realtimeDeltaTicks)

        GAUGE_BG.draw(
            guiGraphics = guiGraphics,
            x = GAUGE_X,
            y = GAUGE_Y,
        )
        guiGraphics.fillImage(ICON_OFF)

        guiGraphics.drawGauge(NITRO_GAUGE, nitroGauge)
        guiGraphics.drawGauge(TEAM_GAUGE, teamBoostGauge)

        if (nitroGauge == 1f) guiGraphics.fillImage(ICON_ON)
    }

    class Builder : HudElementBuilder<Spec>() {


        override fun build(layout: HudLayoutSpec) = Spec(
            layout,
        )
    }

    @Serializable
    @SerialName("JIU_GAUGE")
    data class Spec(
        override val layout: HudLayoutSpec

    ) : HudElementSpec<JiuGauge, NitroEngine> {
        override fun requiredEngineClass() = NitroEngine::class.java

        override fun create(
            kart: KartRef.Specific<NitroEngine>,
            parent: ElementHolder
        ) = JiuGauge(this, kart, parent)

    }
}
