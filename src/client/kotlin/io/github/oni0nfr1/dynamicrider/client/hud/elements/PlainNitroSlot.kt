package io.github.oni0nfr1.dynamicrider.client.hud.elements

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.hud.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.NitroSlot
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType

open class PlainNitroSlot(
    manager: HudStateManager,
    var hide: Boolean = false,
    composer: PlainNitroSlot.() -> Unit
): HudElementImpl<PlainNitroSlot>(manager, composer), NitroSlot {

    override var occupied: Boolean = false

    var iconSize = 32
    var boxPadding = 5
    var boxColor = 0x80000000.toInt()

    override fun resolveSize() {
        val size = iconSize + boxPadding * 2
        setSize(size, size)
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        if (hide) return
        guiGraphics.fill(0, 0, size.x, size.y, boxColor)
        if (occupied) {
            guiGraphics.blit(
                RenderType::guiTextured,
                ResourceStore.boosterIcon,
                boxPadding, boxPadding,
                0f, 0f,
                iconSize, iconSize, iconSize, iconSize
            )
        }
    }

}