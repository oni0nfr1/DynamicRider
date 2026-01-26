package io.github.oni0nfr1.dynamicrider.client.graphics.render

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DestFactor
import com.mojang.blaze3d.platform.SourceFactor
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.resources.ResourceLocation

object DynRiderRenderTypes {
    private val STATE: RenderType.CompositeState =
        RenderType.CompositeState.builder()
            .setTextureState(RenderStateShard.NO_TEXTURE)
            .setOutputState(RenderStateShard.MAIN_TARGET)
            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING_FORWARD)
            .createCompositeState(RenderType.OutlineProperty.NONE)

    private val CORE_PIPELINE: RenderPipeline by lazy {
        buildHudPositionColorPipeline(
            ResourceLocation.fromNamespaceAndPath("dynamicrider", "hud_arc_core"),
            BlendFunction.TRANSLUCENT
        )
    }

    private val CORE_PIPELINE_TRI: RenderPipeline by lazy {
        buildHudPositionColorPipeline(
            ResourceLocation.fromNamespaceAndPath("dynamicrider", "hud_arc_core_tri"),
            BlendFunction.TRANSLUCENT,
            VertexFormat.Mode.TRIANGLES
        )
    }

    private val HALO_PIPELINE: RenderPipeline by lazy {
         buildHudPositionColorPipeline(
           ResourceLocation.fromNamespaceAndPath("dynamicrider", "hud_arc_halo"),
           BlendFunction(
             SourceFactor.SRC_ALPHA, DestFactor.ONE,
             SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA
           )
         )
    }

    val ARC_CORE: RenderType by lazy {
        RenderType.create(
            "dynamicrider:arc_core",
            1536,
            false,
            true,
            CORE_PIPELINE,
            STATE
        )
    }

    val ARC_CORE_TRI: RenderType by lazy {
        RenderType.create(
            "dynamicrider:arc_core_tri",
            1536,
            false,
            true,
            CORE_PIPELINE_TRI,
            STATE
        )
    }

    val ARC_HALO: RenderType by lazy {
        RenderType.create(
            "dynamicrider:arc_halo",
            1536,
            false,
            true,
            HALO_PIPELINE,
            STATE
        )
    }
}
