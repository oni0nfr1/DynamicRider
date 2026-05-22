package io.github.oni0nfr1.dynamicrider.client.graphics.render

import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import org.joml.Matrix4f

inline fun GuiGraphics.batch(
    mode: VertexFormat.Mode,
    vertexFormat: VertexFormat,
    renderType: RenderType,
    builderFunction: BufferBuilder.(Matrix4f) -> Unit,
) {
    val poseMatrix = this.pose().last().pose()
    val tesselator = Tesselator.getInstance()
    val builder = tesselator.begin(mode, vertexFormat)

    builder.builderFunction(poseMatrix)

    val meshData = builder.build()
    renderType.draw(meshData)
}
