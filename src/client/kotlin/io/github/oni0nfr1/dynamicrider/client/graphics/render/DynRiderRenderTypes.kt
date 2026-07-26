package io.github.oni0nfr1.dynamicrider.client.graphics.render

import io.github.oni0nfr1.dynamicrider.client.graphics.render.pipeline.DynRiderRenderPipelines
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.TriState
import java.util.concurrent.ConcurrentHashMap

object DynRiderRenderTypes {
    private val STATE: RenderType.CompositeState =
        RenderType.CompositeState.builder()
            .setTextureState(RenderStateShard.NO_TEXTURE)
            .setOutputState(RenderStateShard.MAIN_TARGET)
            .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING_FORWARD)
            .createCompositeState(RenderType.OutlineProperty.NONE)

    private val hueShiftTypes = ConcurrentHashMap<ResourceLocation, RenderType>()

    val ARC_CORE: RenderType by lazy {
        RenderType.create(
            "dynamicrider:arc_core",
            1536,
            false,
            true,
            DynRiderRenderPipelines.ARC_CORE,
            STATE
        )
    }

    val ARC_CORE_TRI: RenderType by lazy {
        RenderType.create(
            "dynamicrider:arc_core_tri",
            1536,
            false,
            true,
            DynRiderRenderPipelines.ARC_CORE_TRI,
            STATE
        )
    }

    val ARC_HALO: RenderType by lazy {
        RenderType.create(
            "dynamicrider:arc_halo",
            1536,
            false,
            true,
            DynRiderRenderPipelines.ARC_HALO,
            STATE
        )
    }

    /** [texture]를 hue shift shader로 그리는 GUI용 RenderType을 반환한다. */
    fun hueShiftedTextured(texture: ResourceLocation): RenderType = hueShiftTypes.computeIfAbsent(texture) {
        val state = RenderType.CompositeState.builder()
            .setTextureState(RenderStateShard.TextureStateShard(texture, TriState.FALSE, false))
            .setOutputState(RenderStateShard.MAIN_TARGET)
            .createCompositeState(RenderType.OutlineProperty.NONE)
        RenderType.create(
            "dynrider:hue_shifted_textured",
            1536,
            false,
            true,
            DynRiderRenderPipelines.HUE_SHIFT,
            state,
        )
    }
}
