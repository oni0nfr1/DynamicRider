package io.github.oni0nfr1.dynamicrider.client.graphics.render.shape

import io.github.oni0nfr1.dynamicrider.client.graphics.texture.Atlas
import io.github.oni0nfr1.dynamicrider.client.resource.element.data.GaugeFillRegion
import net.minecraft.client.gui.GuiGraphics

fun Atlas.Cell.drawGauge(
    guiGraphics: GuiGraphics,
    region: GaugeFillRegion,
    value: Float,
    x: Int = 0,
    y: Int = 0,
    color: Int = 0xFFFFFFFF.toInt(),
) {
    require(value.isFinite()) { "value must be finite: $value" }
    require(region.x >= 0) { "region.x must be non-negative: ${region.x}" }
    require(region.y >= 0) { "region.y must be non-negative: ${region.y}" }
    require(region.width > 0) { "region.width must be positive: ${region.width}" }
    require(region.height > 0) { "region.height must be positive: ${region.height}" }
    require(region.x + region.width <= width) {
        "gauge region exceeds cell width: regionX=${region.x}, regionWidth=${region.width}, cellWidth=$width"
    }
    require(region.y + region.height <= height) {
        "gauge region exceeds cell height: regionY=${region.y}, regionHeight=${region.height}, cellHeight=$height"
    }

    val progress = value.coerceIn(0f, 1f)

    when (region.direction) {
        GaugeFillRegion.Direction.LEFT_TO_RIGHT -> {
            val filledWidth = (region.width * progress).toInt()
            if (filledWidth <= 0) return

            drawRegion(
                guiGraphics = guiGraphics,
                x = x + region.x,
                y = y + region.y,
                sourceX = region.x,
                sourceY = region.y,
                sourceWidth = filledWidth,
                sourceHeight = region.height,
                color = color,
            )
        }

        GaugeFillRegion.Direction.RIGHT_TO_LEFT -> {
            val filledWidth = (region.width * progress).toInt()
            if (filledWidth <= 0) return
            val offsetX = region.width - filledWidth

            drawRegion(
                guiGraphics = guiGraphics,
                x = x + region.x + offsetX,
                y = y + region.y,
                sourceX = region.x + offsetX,
                sourceY = region.y,
                sourceWidth = filledWidth,
                sourceHeight = region.height,
                color = color,
            )
        }

        GaugeFillRegion.Direction.TOP_TO_BOTTOM -> {
            val filledHeight = (region.height * progress).toInt()
            if (filledHeight <= 0) return

            drawRegion(
                guiGraphics = guiGraphics,
                x = x + region.x,
                y = y + region.y,
                sourceX = region.x,
                sourceY = region.y,
                sourceWidth = region.width,
                sourceHeight = filledHeight,
                color = color,
            )
        }

        GaugeFillRegion.Direction.BOTTOM_TO_TOP -> {
            val filledHeight = (region.height * progress).toInt()
            if (filledHeight <= 0) return
            val offsetY = region.height - filledHeight

            drawRegion(
                guiGraphics = guiGraphics,
                x = x + region.x,
                y = y + region.y + offsetY,
                sourceX = region.x,
                sourceY = region.y + offsetY,
                sourceWidth = region.width,
                sourceHeight = filledHeight,
                color = color,
            )
        }
    }
}
