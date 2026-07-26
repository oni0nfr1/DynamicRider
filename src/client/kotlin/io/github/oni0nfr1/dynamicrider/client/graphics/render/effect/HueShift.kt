package io.github.oni0nfr1.dynamicrider.client.graphics.render.effect

import io.github.oni0nfr1.dynamicrider.client.graphics.render.DynRiderRenderTypes
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import kotlin.math.roundToInt

/** Hue shift shader가 정점 색상으로 읽을 hue와 opacity를 ARGB 색상으로 인코딩한다. */
object HueShift {
    /**
     * [degrees]를 한 바퀴로 정규화해 red 채널에 기록한다.
     *
     * Green과 blue는 흰색으로 유지하고 alpha에는 [opacity]를 기록한다. 0도는 일반 흰색 정점 색상과 같은
     * `0xFFFFFFFF`가 되므로 전용 RenderType에 기본 blit 색상을 전달해도 원본 색상이 유지된다.
     */
    fun encodeVertexColor(degrees: Float, opacity: Int = 255): Int {
        val normalized = normalizeDegrees(degrees)
        val encodedHue = if (normalized == 0f) 255 else (normalized / 360f * 255f).roundToInt()
        return (opacity.coerceIn(0, 255) shl 24) or (encodedHue shl 16) or 0xFFFF
    }

    /** 임의의 각도를 `[0, 360)` 범위로 정규화한다. */
    fun normalizeDegrees(degrees: Float): Float {
        if (!degrees.isFinite()) return 0f
        return ((degrees % 360f) + 360f) % 360f
    }
}

/**
 * 텍스처의 지정 영역을 [hueDegrees]만큼 hue shift해서 그린다.
 *
 * [width]와 [height]는 화면에 그릴 크기이고, [uWidth]와 [vHeight]는 원본 텍스처에서 사용할 영역의 크기다.
 * [opacity]는 `0..255` 범위를 벗어나면 가장 가까운 경곗값으로 보정된다.
 */
fun GuiGraphics.blitHueShifted(
    texture: ResourceLocation,
    x: Int,
    y: Int,
    u: Float,
    v: Float,
    width: Int,
    height: Int,
    uWidth: Int,
    vHeight: Int,
    textureWidth: Int,
    textureHeight: Int,
    hueDegrees: Float,
    opacity: Int = 255,
) {
    blit(
        DynRiderRenderTypes::hueShiftedTextured,
        texture,
        x,
        y,
        u,
        v,
        width,
        height,
        uWidth,
        vHeight,
        textureWidth,
        textureHeight,
        HueShift.encodeVertexColor(hueDegrees, opacity),
    )
}
