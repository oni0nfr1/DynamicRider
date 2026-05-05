package io.github.oni0nfr1.dynamicrider.client.hud.elements.impl

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.spec.HudLayoutSpec
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f

abstract class HudElementImpl<E: KartEngine>(
    layout: HudLayoutSpec,
    val kart: KartRef.Specific<E>,
) : HudElement<E> {
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

        val rx = position.x + screenPoint.x - (elementPoint.x * scale.x)
        val ry = position.y + screenPoint.y - (elementPoint.y * scale.y)

        renderPosition.set(rx, ry, zIndex)
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
