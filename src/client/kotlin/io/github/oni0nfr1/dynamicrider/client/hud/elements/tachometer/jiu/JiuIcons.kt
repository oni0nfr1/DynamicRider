package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.jiu

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.graphics.amination.LoopTimer
import io.github.oni0nfr1.dynamicrider.client.graphics.util.fillImage
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.resource.atlas.AtlasRegistry
import io.github.oni0nfr1.skid.client.api.engine.JiuEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class JiuIcons(
    spec: Spec,
    kart: KartRef.Specific<JiuEngine>,
    parent: ElementHolder,
) : HudElementImpl<JiuEngine>(spec.layout, kart, parent) {

    companion object {
        @Suppress("NOTHING_TO_INLINE")
        inline fun atlasId(path: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(
            ResourceStore.MOD_ID,
            "element/jiu_tachometer/$path"
        )

        val ATLAS by AtlasRegistry.sprite(atlasId("icons"))

        val BACKGROUND get() = ATLAS.cellAt(0, 0)
        val DRAFT      get() = ATLAS.cellAt(1, 0)
        val AUTO_GAUGE get() = ATLAS.cellAt(2, 0)
    }

    override val width: Int
        get() = ATLAS.cellWidth
    override val height: Int
        get() = ATLAS.cellHeight

    val draftActive: Boolean
        get() = kart.accessEngine { it.draftActive } ?: false
    val draftCharging: Boolean
        get() = kart.accessEngine { it.draftCharging } ?: false

    val autoGauge: Boolean
        get() = kart.accessEngine { engine ->
            val speed = engine.tachometer?.speed ?: return@accessEngine null
            val isDrifting = engine.isDrifting
            val isBoosting = engine.isBoosting

            return !isBoosting && !isDrifting && speed.toInt() >= 100
        } ?: false

    val draftBlink = LoopTimer(1000, spec.draftBlinkSpeed)

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        if (draftCharging && !draftBlink.running) draftBlink.start()
        if (!draftActive && draftBlink.running) draftBlink.stop()

        guiGraphics.fillImage(BACKGROUND)

        if (draftActive) guiGraphics.fillImage(DRAFT)
        else if (draftCharging && draftBlink.progress < 0.5f) guiGraphics.fillImage(DRAFT)

        if (autoGauge) guiGraphics.fillImage(AUTO_GAUGE)
    }

    class Builder : HudElementBuilder<Spec>() {
        var draftBlinkSpeed = 1.0

        override fun build(layout: HudLayoutSpec) = Spec(
            layout,
            draftBlinkSpeed,
        )
    }

    @Serializable
    @SerialName("JIU_ICONS")
    data class Spec(
        override val layout: HudLayoutSpec,
        val draftBlinkSpeed: Double,
    ) : HudElementSpec<JiuIcons, JiuEngine> {
        override fun requiredEngineClass() = JiuEngine::class.java

        override fun create(
            kart: KartRef.Specific<JiuEngine>,
            parent: ElementHolder
        ) = JiuIcons(this, kart, parent)
    }
}
