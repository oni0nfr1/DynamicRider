package io.github.oni0nfr1.dynamicrider.client.graphics.util

import kotlinx.serialization.Serializable
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation

open class SpriteAtlas(
    protected val texture: ResourceLocation,
    override val width: Int,
    override val height: Int,
    override val cellWidth: Int,
    override val cellHeight: Int,
) : Atlas {
    init {
        require(width > 0) { "width must be positive: $width" }
        require(height > 0) { "height must be positive: $height" }
        require(cellWidth > 0) { "cellWidth must be positive: $cellWidth" }
        require(cellHeight > 0) { "cellHeight must be positive: $cellHeight" }
        require(width % cellWidth == 0) {
            "width must be divisible by cellWidth: width=$width, cellWidth=$cellWidth"
        }
        require(height % cellHeight == 0) {
            "height must be divisible by cellHeight: height=$height, cellHeight=$cellHeight"
        }
    }

    override val rowCount: Int = height / cellHeight
    override val colCount: Int = width / cellWidth

    override fun cellAt(row: Int, col: Int): Cell {
        require(row in 0 until rowCount) {
            "row must be in 0 until $rowCount: $row"
        }
        require(col in 0 until colCount) {
            "col must be in 0 until $colCount: $col"
        }

        return Cell(
            texture = texture,
            atlasWidth = width,
            atlasHeight = height,
            width = cellWidth,
            height = cellHeight,
            u = col * cellWidth,
            v = row * cellHeight,
        )
    }

    override fun cellAt(pos: Atlas.CellPosition): Cell  = cellAt(pos.row, pos.col)

    open class Cell(
        private val texture: ResourceLocation,
        private val atlasWidth: Int,
        private val atlasHeight: Int,
        override val width: Int,
        override val height: Int,
        val u: Int,
        val v: Int,
    ) : Atlas.Cell {
        override fun draw(
            guiGraphics: GuiGraphics,
            x: Int,
            y: Int,
            color: Int,
        ) {
            draw(
                guiGraphics,
                x = x,
                y = y,
                drawWidth = width,
                drawHeight = height,
                color = color,
            )
        }

        override fun draw(
            guiGraphics: GuiGraphics,
            x: Int,
            y: Int,
            drawWidth: Int,
            drawHeight: Int,
            color: Int,
        ) {
            drawRegion(
                guiGraphics = guiGraphics,
                x = x,
                y = y,
                sourceX = 0,
                sourceY = 0,
                sourceWidth = width,
                sourceHeight = height,
                drawWidth = drawWidth,
                drawHeight = drawHeight,
                color = color,
            )
        }

        override fun drawRegion(
            guiGraphics: GuiGraphics,
            x: Int,
            y: Int,
            sourceX: Int,
            sourceY: Int,
            sourceWidth: Int,
            sourceHeight: Int,
            drawWidth: Int,
            drawHeight: Int,
            color: Int,
        ) {
            require(sourceX >= 0) { "sourceX must be non-negative: $sourceX" }
            require(sourceY >= 0) { "sourceY must be non-negative: $sourceY" }
            require(sourceWidth > 0) { "sourceWidth must be positive: $sourceWidth" }
            require(sourceHeight > 0) { "sourceHeight must be positive: $sourceHeight" }
            require(sourceX + sourceWidth <= width) {
                "source region exceeds cell width: sourceX=$sourceX, sourceWidth=$sourceWidth, cellWidth=$width"
            }
            require(sourceY + sourceHeight <= height) {
                "source region exceeds cell height: sourceY=$sourceY, sourceHeight=$sourceHeight, cellHeight=$height"
            }
            require(drawWidth > 0) { "drawWidth must be positive: $drawWidth" }
            require(drawHeight > 0) { "drawHeight must be positive: $drawHeight" }

            guiGraphics.blit(
                RenderType::guiTextured,
                texture,
                x, y,
                (u + sourceX).toFloat(),
                (v + sourceY).toFloat(),
                drawWidth,
                drawHeight,
                sourceWidth,
                sourceHeight,
                atlasWidth,
                atlasHeight,
                color,
            )
        }

        override fun drawScaled(
            guiGraphics: GuiGraphics,
            x: Int,
            y: Int,
            scaleX: Int,
            scaleY: Int,
            color: Int,
        ) {
            require(scaleX > 0) { "scaleX must be positive: $scaleX" }
            require(scaleY > 0) { "scaleY must be positive: $scaleY" }

            draw(
                guiGraphics = guiGraphics,
                x = x,
                y = y,
                drawWidth = width * scaleX,
                drawHeight = height * scaleY,
                color = color,
            )
        }

        override fun drawScaled(
            guiGraphics: GuiGraphics,
            x: Int,
            y: Int,
            scale: Int,
            color: Int,
        ) {
            drawScaled(
                guiGraphics = guiGraphics,
                x = x,
                y = y,
                scaleX = scale,
                scaleY = scale,
                color = color,
            )
        }
    }
}
