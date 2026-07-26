package io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.x

import io.github.oni0nfr1.dynamicrider.client.animation.LoopTimer
import io.github.oni0nfr1.dynamicrider.client.animation.OneShotTimer
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.Atlas
import io.github.oni0nfr1.dynamicrider.client.graphics.texture.fillImage
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudElementInfo
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudLayout
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.state.XKartState
import io.github.oni0nfr1.dynamicrider.client.resource.ResourceLocationSerializer
import io.github.oni0nfr1.dynamicrider.client.resource.atlas.AtlasRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementMetaData
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.elementId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f
import kotlin.math.PI

class XStatusRing(
    private val spec: Spec,
    context: HudSceneContext<XKartState>,
    parent: ElementHolder,
) : HudElementImpl<XKartState>(spec.layout, context, parent) {

    companion object {
        val META by ElementRegistry.elementMeta<Meta>(
            elementId("x_status_ring/status_ring"),
        )

        val RING_ATLAS get() = AtlasRegistry.requireSprite(META.ringAtlas)
        val EFFECT_FRAGMENT_ATLAS get() = AtlasRegistry.requireSprite(META.effectFragmentAtlas)

        val BACKGROUND get() = RING_ATLAS.cellAt(META.backgroundCell)
        val RING_ON get() = RING_ATLAS.cellAt(META.ringOnCell)
        val EXPANDING_RING get() = RING_ATLAS.cellAt(META.expandingRingCell)
        val DRAFT_ICON get() = RING_ATLAS.cellAt(META.draftIconCell)
        val NITRO_ICON get() = RING_ATLAS.cellAt(META.nitroIconCell)
        val EFFECT_FRAGMENT get() = EFFECT_FRAGMENT_ATLAS.cellAt(META.effectFragmentCell)
    }

    @Serializable
    data class Meta(
        @Serializable(with = ResourceLocationSerializer::class)
        val ringAtlas: ResourceLocation,
        @Serializable(with = ResourceLocationSerializer::class)
        val effectFragmentAtlas: ResourceLocation,
        val backgroundCell: Atlas.CellPosition,
        val ringOnCell: Atlas.CellPosition,
        val expandingRingCell: Atlas.CellPosition,
        val draftIconCell: Atlas.CellPosition,
        val nitroIconCell: Atlas.CellPosition,
        val effectFragmentCell: Atlas.CellPosition,
        val effectCenterX: Float,
        val effectCenterY: Float,
        val effectFragments: List<EffectFragment>,
        val effectDurationMillis: Long,
        val blinkIntervalMillis: Long,
        val expandingRingScale: Float,
    ) : ElementMetaData {
        @Serializable
        data class EffectFragment(
            val delayFraction: Float,
            val startOffsetX: Float,
            val startOffsetY: Float,
            val travelX: Float,
            val travelY: Float,
            val initialRotationDegrees: Float,
            val rotationSpeedDegreesPerSecond: Float,
            val startScaleX: Float,
            val startScaleY: Float,
            val endScaleX: Float,
            val endScaleY: Float,
            val startShearX: Float,
            val endShearX: Float,
        )
    }

    override val width: Int
        get() = RING_ATLAS.cellWidth
    override val height: Int
        get() = RING_ATLAS.cellHeight

    private val dualBoostBlink = LoopTimer(
        META.blinkIntervalMillis,
        spec.blinkSpeed,
        context::nanoTime,
    )
    private val draftBlink = LoopTimer(
        META.blinkIntervalMillis,
        spec.blinkSpeed,
        context::nanoTime,
    )
    private val activationEffect = OneShotTimer(
        META.effectDurationMillis,
        spec.animationSpeed,
        context::nanoTime,
    )

    private var wasDualBoostActive = false
    private val effectTransform = Matrix4f()
    private val shearTransform = Matrix4f()

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker,
    ) {
        updateTimers()

        val activationProgress = activationProgress()
        if (activationProgress != null) {
            drawEffectFragments(guiGraphics, activationProgress)
        }

        guiGraphics.fillImage(BACKGROUND)
        if (dualBoostVisible()) {
            guiGraphics.fillImage(RING_ON)
        }

        if (activationProgress != null) {
            val opacity = effectOpacity(activationProgress)
            drawExpandingRing(
                guiGraphics,
                easeOutCubic(activationProgress),
                opacity,
            )
        }

        if (context.kartState.isBoosting) {
            guiGraphics.fillImage(NITRO_ICON)
        }
        if (draftVisible()) {
            guiGraphics.fillImage(DRAFT_ICON)
        }
    }

    private fun updateTimers() {
        val dualBoostActive = context.kartState.dualBoostActive
        if (dualBoostActive && !wasDualBoostActive) {
            activationEffect.restart()
        }
        wasDualBoostActive = dualBoostActive

        updateBlink(
            timer = dualBoostBlink,
            blinking = context.kartState.dualBoostCharging && !dualBoostActive,
        )
        updateBlink(
            timer = draftBlink,
            blinking = context.kartState.draftCharging && !context.kartState.draftActive,
        )
    }

    private fun updateBlink(timer: LoopTimer, blinking: Boolean) {
        if (blinking && !timer.running) {
            timer.restart()
        } else if (!blinking && timer.running) {
            timer.stop()
        }
    }

    private fun dualBoostVisible(): Boolean =
        context.kartState.dualBoostActive ||
            context.kartState.dualBoostCharging && dualBoostBlink.progress < 0.5f

    private fun draftVisible(): Boolean =
        context.kartState.draftActive ||
            context.kartState.draftCharging && draftBlink.progress < 0.5f

    private fun activationProgress(): Float? {
        val progress = activationEffect.progress
        if (!activationEffect.running && progress >= 1f) return null
        if (!activationEffect.running && progress <= 0f) return null
        if (effectOpacity(progress) <= 0) return null
        return progress
    }

    private fun effectOpacity(progress: Float): Int =
        ((1f - progress) * 255f).toInt().coerceIn(0, 255)

    private fun drawExpandingRing(
        guiGraphics: GuiGraphics,
        progress: Float,
        opacity: Int,
    ) {
        val scale = 1f + (META.expandingRingScale - 1f) * progress
        effectTransform.identity()
            .translate(META.effectCenterX, META.effectCenterY, 0f)
            .scale(scale, scale, 1f)
            .translate(-META.effectCenterX, -META.effectCenterY, 0f)

        val pose = guiGraphics.pose()
        pose.pushPose()
        pose.mulPose(effectTransform)
        EXPANDING_RING.draw(guiGraphics, 0, 0, colorWithAlpha(opacity))
        pose.popPose()
    }

    private fun drawEffectFragments(
        guiGraphics: GuiGraphics,
        progress: Float,
    ) {
        val pose = guiGraphics.pose()
        val durationSeconds = META.effectDurationMillis / 1_000f

        META.effectFragments.forEach { fragment ->
            val delay = fragment.delayFraction.coerceIn(0f, 0.999f)
            if (progress <= delay) return@forEach

            val localLinearProgress = ((progress - delay) / (1f - delay))
                .coerceIn(0f, 1f)
            val localProgress = easeOutCubic(localLinearProgress)
            val centerX = META.effectCenterX +
                fragment.startOffsetX +
                fragment.travelX * localProgress
            val centerY = META.effectCenterY +
                fragment.startOffsetY +
                fragment.travelY * localProgress
            val scaleX = lerp(fragment.startScaleX, fragment.endScaleX, localProgress)
            val scaleY = lerp(fragment.startScaleY, fragment.endScaleY, localProgress)
            val shearX = lerp(fragment.startShearX, fragment.endShearX, localProgress)
            val elapsedSeconds = (progress - delay) * durationSeconds
            val rotationDegrees = fragment.initialRotationDegrees +
                fragment.rotationSpeedDegreesPerSecond *
                elapsedSeconds
            val opacity = ((1f - localLinearProgress) * 255f)
                .toInt()
                .coerceIn(0, 255)
            if (opacity <= 0) return@forEach

            effectTransform.identity()
                .translate(centerX, centerY, 0f)
                .rotateZ(rotationDegrees / 180f * PI.toFloat())
            shearTransform.identity().m10(shearX)
            effectTransform.mul(shearTransform)
                .scale(scaleX, scaleY, 1f)
                .translate(
                    -EFFECT_FRAGMENT.width / 2f,
                    -EFFECT_FRAGMENT.height / 2f,
                    0f,
                )

            pose.pushPose()
            pose.mulPose(effectTransform)
            EFFECT_FRAGMENT.draw(guiGraphics, 0, 0, colorWithAlpha(opacity))
            pose.popPose()
        }
    }

    private fun lerp(start: Float, end: Float, progress: Float): Float =
        start + (end - start) * progress

    private fun easeOutCubic(value: Float): Float {
        val inverse = 1f - value.coerceIn(0f, 1f)
        return 1f - inverse * inverse * inverse
    }

    private fun colorWithAlpha(alpha: Int): Int =
        (alpha.coerceIn(0, 255) shl 24) or 0x00FFFFFF

    @Serializable
    @SerialName("X_STATUS_RING")
    @HudElementInfo(category = "tachometer")
    data class Spec(
        @HudLayout
        override val layout: HudLayoutSpec = HudLayoutSpec(),
        val blinkSpeed: Double = 3.0,
        val animationSpeed: Double = 1.0,
    ) : HudElementSpec<XStatusRing, XKartState> {
        override fun requiredStateClass() = XKartState::class.java

        override fun create(
            context: HudSceneContext<XKartState>,
            parent: ElementHolder,
        ) = XStatusRing(this, context, parent)
    }
}
