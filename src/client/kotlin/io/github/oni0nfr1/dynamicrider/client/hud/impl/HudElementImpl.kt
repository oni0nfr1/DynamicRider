package io.github.oni0nfr1.dynamicrider.client.hud.impl

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector3f

typealias Composer<T> = T.() -> Unit

/**
 * T: 이 클래스를 상속하는 클래스 자신의 타입을 넘겨주면 됨
 *
 * composer는 해당 클래스의 모든 프로퍼티에 접근할 필요가 있으므로,
 * 클래스 선언시에 T를 넘겨주어 그 프로퍼티에 접근할 수 있게 만듬
 */
abstract class HudElementImpl<T: HudElement>(
    val stateManager: HudStateManager,
    val composer: Composer<T>
): HudElement {

    override var screenAnchor: HudAnchor = HudAnchor.TOP_LEFT
    override var elementAnchor: HudAnchor = HudAnchor.TOP_LEFT
    override var scale: Vector2f = Vector2f(1f, 1f)
    override var position: Vector2i = Vector2i(0, 0)
    override var zIndex: Float = 0f

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
        @Suppress("UNCHECKED_CAST")
        stateManager.recomposeIfDirty(this) {
            composer.invoke(this as T)
            this.resolveSize()
        }

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