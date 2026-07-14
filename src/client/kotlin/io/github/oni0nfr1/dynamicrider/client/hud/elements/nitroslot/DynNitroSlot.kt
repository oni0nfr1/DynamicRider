package io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot

import io.github.oni0nfr1.dynamicrider.client.graphics.amination.OneShotTimer
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.bridge.NitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.bridge.SkidNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HexColorSerdes
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

class DynNitroSlot(
    spec: Spec,
    context: HudSceneContext<NitroKartState>,
    parent: ElementHolder,
) : HudElementImpl<NitroKartState>(spec.layout, context, parent),
    NitroSlot by SkidNitroSlot(context.kartState)
{

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

    val convertAnim = OneShotTimer(1000, nanoTime = context::nanoTime)

    var wasTeamBoost = false

    override val width: Int = SIZE_X
    override val height: Int = SIZE_Y

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
        if (maxBoost < this@DynNitroSlot.slotIndex) return

        guiGraphics.fillImageColored(BACKGROUND, backgroundColor)
        guiGraphics.fillImage(FRAME)

        if (nitro >= this@DynNitroSlot.slotIndex) {
            if (teamNitro >= this@DynNitroSlot.slotIndex) {
                if (!wasTeamBoost) onTeamBoostChange()
                guiGraphics.fillImage(teamNitroIconWithAnim)
            } else {
                wasTeamBoost = false
                guiGraphics.fillImage(NITRO_ICON)
            }
        } else wasTeamBoost = false
    }

    @Serializable
    @SerialName("NITRO_SLOT")
    data class Spec(
        override val layout: HudLayoutSpec = HudLayoutSpec(),
        val slotIndex: Int = 1,
        @Serializable(with = HexColorSerdes::class)
        val backgroundColor: Int = 0xFF0000FF.toInt(),
    ) : HudElementSpec<DynNitroSlot, NitroKartState> {
        override fun requiredStateClass() = NitroKartState::class.java
        override fun create(context: HudSceneContext<NitroKartState>, parent: ElementHolder) =
            DynNitroSlot(this, context, parent)
    }
}
