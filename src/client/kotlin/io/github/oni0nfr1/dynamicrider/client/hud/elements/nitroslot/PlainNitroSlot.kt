package io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HexColorSerdes
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

class PlainNitroSlot(
    spec: Spec,
    kart: KartRef.Specific<NitroEngine>,
    parent: ElementHolder,
) : HudElementImpl<NitroEngine>(spec.layout, kart, parent) {

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
        val nitro = kart.accessEngine { engine ->
            engine.tachometer?.nitro
        } ?: 0
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
    data class Spec(
        override val layout: HudLayoutSpec,
        val slotIndex: Int = 1,
        val hideUntilOccupied: Boolean = false,
        val keepVisibleAfterOccupied: Boolean = true,
        val iconSize: Int = 32,
        val boxPadding: Int = 5,
        @Serializable(with = HexColorSerdes::class)
        val boxColor: Int = 0x80000000.toInt(),
    ) : HudElementSpec<PlainNitroSlot, NitroEngine> {
        override fun requiredEngineClass(): Class<out NitroEngine> = NitroEngine::class.java

        override fun create(kart: KartRef.Specific<NitroEngine>, parent: ElementHolder): PlainNitroSlot =
            PlainNitroSlot(this, kart, parent)
    }

}
