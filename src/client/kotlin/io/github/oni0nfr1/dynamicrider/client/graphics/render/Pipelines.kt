package io.github.oni0nfr1.dynamicrider.client.graphics.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.platform.LogicOp
import com.mojang.blaze3d.platform.PolygonMode
import com.mojang.blaze3d.shaders.UniformType
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.resources.ResourceLocation

fun buildHudPositionColorPipeline(
    pipelineId: ResourceLocation,
    blend: BlendFunction?,
    mode: VertexFormat.Mode = VertexFormat.Mode.QUADS,
): RenderPipeline {
    val shaderId = ResourceLocation.withDefaultNamespace("core/position_color")

    val builder = RenderPipeline.builder()
        .withLocation(pipelineId)
        .withVertexShader(shaderId)
        .withFragmentShader(shaderId)
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, mode)

        .withUniform("ModelViewMat", UniformType.MATRIX4X4)
        .withUniform("ProjMat", UniformType.MATRIX4X4)
        .withUniform("ColorModulator", UniformType.VEC4)

        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withCull(false)
        .withPolygonMode(PolygonMode.FILL)
        .withColorLogic(LogicOp.NONE)
        .withColorWrite(true, true)
        .withDepthWrite(false)

    if (blend != null) builder.withBlend(blend) else builder.withoutBlend()
    return builder.build()
}
