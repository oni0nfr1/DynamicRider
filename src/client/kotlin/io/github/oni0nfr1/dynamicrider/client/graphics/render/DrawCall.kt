package io.github.oni0nfr1.dynamicrider.client.graphics.render

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f
import java.util.OptionalDouble
import java.util.OptionalInt

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

/**
 * [pipeline]으로 즉시 draw call을 실행하고 [configurePass]에서 draw별 uniform을 설정할 수 있게 한다.
 *
 * 일반 [RenderType] 경로와 달리 생성된 [RenderPass]를 호출자에게 노출하므로, 파이프라인에 선언된 커스텀
 * uniform을 `setUniform`으로 설정할 수 있다. [samplers]의 키는 파이프라인에 선언된 sampler 이름이어야
 * 한다.
 */
inline fun GuiGraphics.batch(
    pipeline: RenderPipeline,
    samplers: Map<String, ResourceLocation> = emptyMap(),
    target: RenderTarget = Minecraft.getInstance().mainRenderTarget,
    configurePass: RenderPass.() -> Unit = {},
    builderFunction: BufferBuilder.(Matrix4f) -> Unit,
) {
    val poseMatrix = pose().last().pose()
    val builder = Tesselator.getInstance().begin(
        pipeline.vertexFormatMode,
        pipeline.vertexFormat,
    )
    builder.builderFunction(poseMatrix)

    val meshData = builder.build() ?: return
    meshData.use {
        val vertexBuffer = pipeline.vertexFormat.uploadImmediateVertexBuffer(meshData.vertexBuffer())
        val indexBuffer: com.mojang.blaze3d.buffers.GpuBuffer
        val indexType: VertexFormat.IndexType

        val meshIndexBuffer = meshData.indexBuffer()
        if (meshIndexBuffer == null) {
            val sequentialBuffer = RenderSystem.getSequentialBuffer(meshData.drawState().mode())
            indexBuffer = sequentialBuffer.getBuffer(meshData.drawState().indexCount())
            indexType = sequentialBuffer.type()
        } else {
            indexBuffer = pipeline.vertexFormat.uploadImmediateIndexBuffer(meshIndexBuffer)
            indexType = meshData.drawState().indexType()
        }

        // getTexture may upload a texture through the command encoder on first use. Resolve every
        // sampler before opening this draw's render pass so that upload never runs inside a pass.
        val textureManager = Minecraft.getInstance().textureManager
        val resolvedSamplers = samplers.mapValues { (_, texture) ->
            textureManager.getTexture(texture).texture
        }
        val depthTexture = if (target.useDepth) target.depthTexture else null
        RenderSystem.getDevice().createCommandEncoder().createRenderPass(
            target.colorTexture,
            OptionalInt.empty(),
            depthTexture,
            OptionalDouble.empty(),
        ).use { pass ->
            pass.setPipeline(pipeline)
            pass.setVertexBuffer(0, vertexBuffer)
            pass.setIndexBuffer(indexBuffer, indexType)

            if (RenderSystem.SCISSOR_STATE.isEnabled) {
                pass.enableScissor(RenderSystem.SCISSOR_STATE)
            }

            for ((name, texture) in resolvedSamplers) {
                pass.bindSampler(name, texture)
            }

            pass.configurePass()
            pass.drawIndexed(0, meshData.drawState().indexCount())
        }
    }
}
