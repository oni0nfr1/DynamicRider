package io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.NitroSlot
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType

class PlainNitroSlot(
    spec: PlainNitroSlotSpec,
    kart: KartRef.Specific<NitroEngine>
) : HudElementImpl<NitroEngine>(spec.layout, kart), NitroSlot {
    var slotIndex: Int = spec.slotIndex
    var hideUntilOccupied: Boolean = spec.hideUntilOccupied
    var keepVisibleAfterOccupied: Boolean = spec.keepVisibleAfterOccupied
    var iconSize: Int = spec.iconSize
    var boxPadding: Int = spec.boxPadding
    var boxColor: Int = spec.boxColor

    override var occupied: Boolean = false

    private var hasEverBeenOccupied: Boolean = false

    override fun resolveSize() {
        syncState()
        val boxSize = iconSize + boxPadding * 2
        setSize(boxSize, boxSize)
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker,
    ) {
        syncState()
        if (shouldHide()) return

        guiGraphics.fill(0, 0, size.x, size.y, boxColor)
        if (occupied) {
            guiGraphics.blit(
                RenderType::guiTextured,
                ResourceStore.boosterIcon,
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
}
