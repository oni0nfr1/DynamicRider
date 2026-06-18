package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.graphics.util.Atlas
import io.github.oni0nfr1.dynamicrider.client.graphics.util.NumberAtlas
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedmeter.SpeedMeter
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedmeter.impl.SpdMeterImpl
import io.github.oni0nfr1.skid.client.api.engine.SpeedEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class JiuSpdMeter(
    spec: Spec,
    kart: KartRef.Specific<SpeedEngine>,
    parent: ElementHolder
) : HudElementImpl<SpeedEngine>(spec.layout, kart, parent),
    SpeedMeter by SpdMeterImpl(kart) {
    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun id(path: String) = ResourceLocation.fromNamespaceAndPath(
            ResourceStore.MOD_ID,
            "textures/element/jiu_tachometer/$path"
        )

        val BG_ATLAS = Atlas(
            id(""),
            286, 816,
            286, 102
        )

        val BG_ANIM_IMPACT = BG_ATLAS.cellAt(0, 0)
        val BG_ANIM = buildList {
            for (i in 1..5) add(BG_ATLAS.cellAt(i, 0))
        }

        val BACKGROUND_LIGHT = BG_ATLAS.cellAt(6, 0)
        val BACKGROUND_NO_LIGHT = BG_ATLAS.cellAt(7, 0)

        const val NUMBER_WIDTH = 35
        const val NUMBER_HEIGHT = 43
        const val NUMBER_POS_X = 196
        const val NUMBER_POS_Y = 92
        val NUMBER_WHITE = NumberAtlas(
            id("number_white"),
            NUMBER_WIDTH,
            NUMBER_HEIGHT,
        )
        val NUMBER_CYAN = NumberAtlas(
            id("number_cyan"),
            NUMBER_WIDTH,
            NUMBER_HEIGHT,
        )
    }

    override var width: Int = BG_ATLAS.cellWidth
    override var height: Int = BG_ATLAS.cellHeight

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {

    }

    data class Spec(
        override val layout: HudLayoutSpec,

    ) : HudElementSpec<JiuSpdMeter, SpeedEngine>() {
        override fun requiredEngineClass() = SpeedEngine::class.java

        override fun create(
            kart: KartRef.Specific<SpeedEngine>,
            parent: ElementHolder
        ): HudElement<SpeedEngine> {
            TODO("Not yet implemented")
        }
    }
}