package io.github.oni0nfr1.dynamicrider.client.hud.elements.impl

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.layout.HudLayoutEngine
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f

abstract class HudElementImpl<E: KartEngine>(
    layout: HudLayoutSpec,
    val kart: KartRef.Specific<E>,
    protected val parent: ElementHolder,
) : HudElement<E> {
    override var screenAnchor: HudAnchor = layout.screenAnchor
    override var elementAnchor: HudAnchor = layout.elementAnchor
    override var scale: Vector2f = layout.toScale()
    override var position: Vector2i = layout.toPosition()
    override var zIndex: Float = layout.zIndex

    abstract val width: Int

    abstract val height: Int

    private val transform = Matrix4f()
    private val renderPosition = Vector3f()

    private fun updateTransform() {
        transform.identity()
        transform.translate(renderPosition)
        transform.scale(scale.x, scale.y, 1f)
    }

    final override fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        updateLayout()

        val result = HudLayoutEngine.resolve(
            layout = HudLayoutSpec(
                screenAnchor = screenAnchor,
                elementAnchor = elementAnchor,
                scaleX = scale.x,
                scaleY = scale.y,
                x = position.x,
                y = position.y,
                zIndex = zIndex,
            ),
            parentWidth = parent.width,
            parentHeight = parent.height,
            elementWidth = width,
            elementHeight = height,
        )

        renderPosition.set(result.renderX, result.renderY, result.zIndex)
        updateTransform()

        val pose = guiGraphics.pose()
        pose.pushPose()
        pose.mulPose(transform)

        render(guiGraphics, deltaTracker)

        pose.popPose()
    }

    protected open fun updateLayout() {}
    abstract fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker)
}
