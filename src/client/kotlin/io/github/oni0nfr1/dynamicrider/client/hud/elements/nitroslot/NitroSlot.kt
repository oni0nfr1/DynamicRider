package io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot

import io.github.oni0nfr1.dynamicrider.client.graphics.amination.OneShotTimer
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.dsl.HudElementBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HexColorSerdes
import io.github.oni0nfr1.dynamicrider.client.rider.backend.inventory.KartTeamBoostCounter
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

class NitroSlot(
    spec: Spec,
    kart: KartRef.Specific<NitroEngine>,
    parent: ElementHolder,
) : HudElementImpl<NitroEngine>(spec.layout, kart, parent) {

    companion object {
        fun img(name: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(
            "dynrider",
            "textures/element/nitro_slot/$name.png"
        )

        const val SIZE_X = 80
        const val SIZE_Y = 80

        val BACKGROUND = img("background")
        val FRAME = img("frame")
        val NITRO_ICON = img("nitro")
        val TEAM_NITRO_ICON = img("team_nitro")
    }

    val teamNitroIconWithAnim: ResourceLocation
        get() = when ((convertAnim.progress * 8).toInt() % 2) {
            0 -> TEAM_NITRO_ICON
            1 -> NITRO_ICON
            else -> NITRO_ICON
        }

    val backgroundColor = spec.backgroundColor
    val slotIndex = spec.slotIndex

    val convertAnim = OneShotTimer(1000)

    val maxBoost: Int
        get() = kart.accessEngine { it.maxBoost } ?: 2
    val nitro: Int
        get() = kart.accessEngine { it.tachometer?.nitro } ?: 0
    val teamNitro: Int
        get() = KartTeamBoostCounter.boostCount

    var wasTeamBoost = false

    override var width: Int = SIZE_X
    override var height: Int = SIZE_Y

    fun GuiGraphics.fillImage(image: ResourceLocation) {
        blit(
            RenderType::guiTextured,
            image,
            0,
            0,
            0f,
            0f,
            width,
            height,
            width,
            height,
        )
    }

    fun GuiGraphics.fillImageColored(image: ResourceLocation, color: Int) {
        blit(
            RenderType::guiTextured,
            image,
            0,
            0,
            0f,
            0f,
            width,
            height,
            width,
            height,
            color,
        )
    }

    fun onTeamBoostChange() {
        convertAnim.start()
        wasTeamBoost = true
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        if (maxBoost < this@NitroSlot.slotIndex) return

        guiGraphics.fillImageColored(BACKGROUND, backgroundColor)
        guiGraphics.fillImage(FRAME)

        if (nitro >= this@NitroSlot.slotIndex) {
            if (teamNitro >= this@NitroSlot.slotIndex) {
                if (!wasTeamBoost) onTeamBoostChange()
                guiGraphics.fillImage(teamNitroIconWithAnim)
            } else {
                wasTeamBoost = false
                guiGraphics.fillImage(NITRO_ICON)
            }
        } else wasTeamBoost = false
    }

    class Builder : HudElementBuilder<Spec>() {
        var slotIndex: Int = 0
        var backgroundColor: Int = 0xFF808080.toInt()

        override fun build(layout: HudLayoutSpec) =
            Spec(
                layout,
                slotIndex.coerceAtLeast(1),
                backgroundColor,
            )
    }

    @Serializable
    @SerialName("NITRO_SLOT")
    data class Spec(
        override val layout: HudLayoutSpec,
        val slotIndex: Int,
        @Serializable(with = HexColorSerdes::class)
        val backgroundColor: Int,
    ) : HudElementSpec<NitroSlot, NitroEngine>() {
        override fun requiredEngineClass() = NitroEngine::class.java
        override fun create(kart: KartRef.Specific<NitroEngine>, parent: ElementHolder) =
            NitroSlot(this, kart, parent)
    }
}
