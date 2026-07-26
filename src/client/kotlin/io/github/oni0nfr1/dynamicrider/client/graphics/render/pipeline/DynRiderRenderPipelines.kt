package io.github.oni0nfr1.dynamicrider.client.graphics.render.pipeline

import com.mojang.blaze3d.pipeline.BlendFunction
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DestFactor
import com.mojang.blaze3d.platform.SourceFactor
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.resources.ResourceLocation

/**
 * DynamicRider 렌더링 구현에서 공유하는 원시 파이프라인 레지스트리.
 *
 * 외부 호출자는 파이프라인 자체에 의존하지 않고 `DynRiderRenderTypes` 또는 `blit*` 계열의 고수준
 * API를 사용해야 한다.
 */
internal object DynRiderRenderPipelines {
    val ARC_CORE: RenderPipeline by lazy {
        buildHudPositionColorPipeline(
            ResourceLocation.fromNamespaceAndPath("dynrider", "hud_arc_core"),
            BlendFunction.TRANSLUCENT,
        )
    }

    val ARC_CORE_TRI: RenderPipeline by lazy {
        buildHudPositionColorPipeline(
            ResourceLocation.fromNamespaceAndPath("dynrider", "hud_arc_core_tri"),
            BlendFunction.TRANSLUCENT,
            VertexFormat.Mode.TRIANGLES,
        )
    }

    val ARC_HALO: RenderPipeline by lazy {
        buildHudPositionColorPipeline(
            ResourceLocation.fromNamespaceAndPath("dynrider", "hud_arc_halo"),
            BlendFunction(
                SourceFactor.SRC_ALPHA,
                DestFactor.ONE,
                SourceFactor.ONE,
                DestFactor.ONE_MINUS_SRC_ALPHA,
            ),
        )
    }

    val HUE_SHIFT: RenderPipeline by lazy {
        buildHudHueShiftPipeline(ResourceLocation.fromNamespaceAndPath("dynrider", "hud_hue_shift"))
    }

    val ARC_CLIP: RenderPipeline by lazy {
        buildHudArcClipPipeline(ResourceLocation.fromNamespaceAndPath("dynrider", "hud_arc_clip"))
    }
}
