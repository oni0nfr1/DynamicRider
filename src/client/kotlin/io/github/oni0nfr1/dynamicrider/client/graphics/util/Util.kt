package io.github.oni0nfr1.dynamicrider.client.graphics.util

import net.minecraft.client.gui.GuiGraphics

fun GuiGraphics.fillImage(img: Atlas.Cell) {
    img.draw(
        guiGraphics = this,
        x = 0, y = 0,
        drawWidth = img.width,
        drawHeight = img.height,
    )
}