package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.impl

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f

abstract class HudElementImpl(
    layout: HudLayoutSpec,
) : HudElement {
    override var screenAnchor: HudAnchor = layout.screenAnchor
    override var elementAnchor: HudAnchor = layout.elementAnchor
    override var scale: Vector2f = layout.toScale()
    override var position: Vector2i = layout.toPosition()
    override var zIndex: Float = layout.zIndex

    private val transform = Matrix4f()
    protected val size = Vector2i()
    private val renderPosition = Vector3f()

    protected fun setSize(width: Int, height: Int) {
        size.set(width, height)
    }

    private fun updateTransform() {
        transform.identity()
        transform.translate(renderPosition)
        transform.scale(scale.x, scale.y, 1f)
    }

    override fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        resolveSize()

        val window = Minecraft.getInstance().window
        val screenWidth = window.guiScaledWidth
        val screenHeight = window.guiScaledHeight

        val screenPoint = screenAnchor.point(screenWidth, screenHeight)
        val elementPoint = elementAnchor.point(size.x, size.y)

        val rx = screenPoint.x - elementPoint.x + position.x
        val ry = screenPoint.y - elementPoint.y + position.y

        renderPosition.set(
            rx.toFloat(),
            ry.toFloat(),
            zIndex
        )
        updateTransform()

        val pose = guiGraphics.pose()
        pose.pushPose()
        pose.mulPose(transform)

        render(guiGraphics, deltaTracker)

        pose.popPose()
    }

    abstract fun resolveSize()
    abstract fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker)
}
