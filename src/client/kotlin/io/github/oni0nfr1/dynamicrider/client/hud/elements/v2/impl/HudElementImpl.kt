package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.impl

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl.ElementDataBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl.ElementLayoutBuilder
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f

abstract class HudElementImpl<BUILDER, SELF>(
    builder: BUILDER
)
    where
        BUILDER : ElementDataBuilder<BUILDER, SELF>,
        SELF : HudElementImpl<BUILDER, SELF>
{
    private var screenAnchor: HudAnchor = builder.layout.screenAnchor
    private var elementAnchor: HudAnchor = builder.layout.elementAnchor
    private var scale: Vector2f = builder.layout.scale
    private var position: Vector2i = builder.layout.position
    private var zIndex: Float = builder.layout.zIndex

    init {
        setLayout(builder.layout)
    }

    fun setLayout(layout: ElementLayoutBuilder) {
        screenAnchor = layout.screenAnchor
        elementAnchor = layout.elementAnchor
        scale = layout.scale
        position = layout.position
        zIndex = layout.zIndex
    }

    private val transform = Matrix4f()
    protected val size = Vector2i()
    private val renderPosition = Vector3f()

    private fun updateTransform() {
        transform.identity()
        transform.translate(renderPosition)
        transform.scale(scale.x, scale.y, 1f)
    }

    fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
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