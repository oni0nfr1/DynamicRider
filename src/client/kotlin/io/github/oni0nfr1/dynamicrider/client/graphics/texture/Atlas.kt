package io.github.oni0nfr1.dynamicrider.client.graphics.texture

import kotlinx.serialization.Serializable
import net.minecraft.client.gui.GuiGraphics

interface Atlas {

    val width: Int
    val height: Int
    val cellWidth: Int
    val cellHeight: Int

    val rowCount: Int
    val colCount: Int

    fun cellAt(row: Int, col: Int): Cell

    fun cellAt(pos: CellPosition): Cell

    @Serializable
    data class CellPosition(
        val row: Int,
        val col: Int,
    )

    interface Cell {
        val width: Int
        val height: Int

        fun draw(
            guiGraphics: GuiGraphics,
            x: Int,
            y: Int,
            color: Int = 0xFFFFFFFF.toInt()
        )

        fun drawRegion(
            guiGraphics: GuiGraphics,
            x: Int,
            y: Int,
            sourceX: Int,
            sourceY: Int,
            sourceWidth: Int,
            sourceHeight: Int,
            drawWidth: Int = sourceWidth,
            drawHeight: Int = sourceHeight,
            color: Int = 0xFFFFFFFF.toInt(),
        )

        fun draw(
            guiGraphics: GuiGraphics,
            x: Int,
            y: Int,
            drawWidth: Int,
            drawHeight: Int,
            color: Int = 0xFFFFFFFF.toInt(),
        )

        fun drawScaled(
            guiGraphics: GuiGraphics,
            x: Int,
            y: Int,
            scaleX: Int,
            scaleY: Int,
            color: Int = 0xFFFFFFFF.toInt(),
        )

        fun drawScaled(
            guiGraphics: GuiGraphics,
            x: Int,
            y: Int,
            scale: Int,
            color: Int = 0xFFFFFFFF.toInt(),
        )
    }
}
