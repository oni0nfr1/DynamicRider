package io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudColor
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudElementInfo
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudLayout
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudProperty
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudRange
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HexColorSerdes
import io.github.oni0nfr1.dynamicrider.client.hud.state.NitroKartState
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

class PlainNitroSlot(
    spec: Spec,
    context: HudSceneContext<NitroKartState>,
    parent: ElementHolder,
) : HudElementImpl<NitroKartState>(spec.layout, context, parent) {

    companion object {
        val BOOST_ICON = ResourceLocation.fromNamespaceAndPath(
            "dynrider",
            "textures/gui/boost_icon.png"
        )
    }

    var slotIndex: Int = spec.slotIndex
    var hideUntilOccupied: Boolean = spec.hideUntilOccupied
    var keepVisibleAfterOccupied: Boolean = spec.keepVisibleAfterOccupied
    var iconSize: Int = spec.iconSize
    var boxPadding: Int = spec.boxPadding
    var boxColor: Int = spec.boxColor

    var occupied: Boolean = false

    private var hasEverBeenOccupied: Boolean = false

    override val width: Int
        get() = iconSize + boxPadding * 2
    override val height: Int
        get() = iconSize + boxPadding * 2

    override fun updateLayout() {
        syncState()
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker,
    ) {
        syncState()
        if (shouldHide()) return

        guiGraphics.fill(0, 0, width, height, boxColor)
        if (occupied) {
            guiGraphics.blit(
                RenderType::guiTextured,
                BOOST_ICON,
                boxPadding,
                boxPadding,
                0f,
                0f,
                iconSize,
                iconSize,
                iconSize,
                iconSize,
            )
        }
    }

    private fun syncState() {
        val nitro = context.kartState.nitro
        occupied = nitro >= slotIndex
        if (occupied) {
            hasEverBeenOccupied = true
        }
    }

    private fun shouldHide(): Boolean {
        if (!hideUntilOccupied) return false
        if (keepVisibleAfterOccupied) return !hasEverBeenOccupied
        return !occupied
    }

    @Serializable
    @SerialName("PLAIN_NITRO_SLOT")
    @HudElementInfo(category = "nitro")
    data class Spec(
        @HudLayout
        override val layout: HudLayoutSpec,
        @HudProperty(
            descriptionKey = "dynamicrider.hud.element.plain_nitro_slot.property.slot_index.description",
        )
        @HudRange(min = 1.0, max = 16.0, step = 1.0)
        val slotIndex: Int = 1,
        val hideUntilOccupied: Boolean = false,
        val keepVisibleAfterOccupied: Boolean = true,
        @HudRange(min = 1.0, max = 256.0, step = 1.0)
        val iconSize: Int = 32,
        @HudRange(min = 0.0, max = 64.0, step = 1.0)
        val boxPadding: Int = 5,
        @HudColor
        @Serializable(with = HexColorSerdes::class)
        val boxColor: Int = 0x80000000.toInt(),
    ) : HudElementSpec<PlainNitroSlot, NitroKartState> {
        override fun requiredStateClass(): Class<out NitroKartState> = NitroKartState::class.java

        override fun create(context: HudSceneContext<NitroKartState>, parent: ElementHolder): PlainNitroSlot =
            PlainNitroSlot(this, context, parent)
    }

}
