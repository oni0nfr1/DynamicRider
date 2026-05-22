package io.github.oni0nfr1.dynamicrider.client.graphics.util

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import io.github.oni0nfr1.dynamicrider.client.graphics.geometry.buildRoundedOutlinePoints
import io.github.oni0nfr1.dynamicrider.client.graphics.render.DynRiderRenderTypes
import io.github.oni0nfr1.dynamicrider.client.graphics.render.batch
import io.github.oni0nfr1.dynamicrider.client.util.colorFromARGB
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix4f
import org.joml.Vector2f

fun GuiGraphics.fillRoundedTrapezoid(
    x: Float,
    y: Float,
    topWidth: Float,
    bottomWidth: Float,
    height: Float,
    cornerRadius: Float,
    argb: Int,
    arcSegments: Int = 6,
) {
    val dx = (topWidth - bottomWidth) * 0.5f

    val topLeft = Vector2f(x, y)
    val topRight = Vector2f(x + topWidth, y)
    val bottomRight = Vector2f(x + dx + bottomWidth, y + height)
    val bottomLeft = Vector2f(x + dx, y + height)

    val polygon = listOf(topLeft, topRight, bottomRight, bottomLeft)
    val outline = buildRoundedOutlinePoints(polygon, cornerRadius, arcSegments)
    if (outline.size < 3) return

    var centerX = 0f
    var centerY = 0f
    for (p in outline) { centerX += p.x; centerY += p.y }
    centerX /= outline.size.toFloat()
    centerY /= outline.size.toFloat()

    val alpha = (argb ushr 24) and 0xFF
    val red   = (argb ushr 16) and 0xFF
    val green = (argb ushr 8) and 0xFF
    val blue  = (argb) and 0xFF
    val color = colorFromARGB(alpha, red, green, blue)

    this.batch(
        VertexFormat.Mode.TRIANGLES,
        DefaultVertexFormat.POSITION_COLOR,
        DynRiderRenderTypes.ARC_CORE_TRI
    ) { poseMatrix: Matrix4f ->
        val n = outline.size
        for (i in 0 until n) {
            val p0 = outline[i]
            val p1 = outline[(i + 1) % n]

            addVertex(poseMatrix, centerX, centerY, 0f)
                .setColor(color)
            addVertex(poseMatrix, p0.x, p0.y, 0f)
                .setColor(color)
            addVertex(poseMatrix, p1.x, p1.y, 0f)
                .setColor(color)
        }
    }
}