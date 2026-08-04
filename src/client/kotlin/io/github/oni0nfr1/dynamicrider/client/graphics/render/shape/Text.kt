package io.github.oni0nfr1.dynamicrider.client.graphics.render.shape

import io.github.oni0nfr1.dynamicrider.client.config.DynRiderConfig
import io.github.oni0nfr1.dynamicrider.client.util.dsegText
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import org.joml.Matrix4f

fun dynriderText(text: String): MutableComponent =
    Component.literal(text).setStyle(DynRiderConfig.hudFont.style)

fun Font.widthWithDynriderFont(text: String): Int = width(dynriderText(text))

fun GuiGraphics.drawScaledText(
    x: Int,
    y: Int,
    scaleFactor: Float,
    text: String,
    argbColor: Int = 0xFFFFFFFF.toInt(),
    shadow: Boolean = true
) {
    val pose = this.pose()
    val transform = Matrix4f()
    transform.translate(x.toFloat(), y.toFloat(), 0f)
    transform.scale(scaleFactor, scaleFactor, 1f)

    pose.pushPose()
    pose.mulPose(transform)

    this.textWithDynriderFont(0, 0, argbColor, text, shadow)
    pose.popPose()
}

fun GuiGraphics.textWithDynriderFont(
    x: Int,
    y: Int,
    argbColor: Int,
    text: String,
    shadow: Boolean = false
) {
    val client = Minecraft.getInstance()
    this.drawString(client.font, dynriderText(text), x, y, argbColor, shadow)
}

fun GuiGraphics.drawSpeed7Seg(
    xCenter: Float,
    yTop: Float,
    speed: Int,
    onArgb: Int,
    offArgb: Int = 0x60000000,
    unitText: String,
    unitPadding: Float = 4f,
    shadow: Boolean = false,
) {
    val font = Minecraft.getInstance().font

    val base = dsegText("888")
    val boxWidth = font.width(base).toFloat()
    val rightEdge = xCenter + boxWidth * 0.5f
    val leftEdge = rightEdge - boxWidth

    drawString(font, base, leftEdge.toInt(), yTop.toInt(), offArgb, shadow)

    val digits = if (speed in 0..999) speed.toString() else speed.toString()
    val comp = dsegText(digits)
    val digitsWidth = font.width(comp).toFloat()
    val xLeft = rightEdge - digitsWidth

    drawString(font, comp, xLeft.toInt(), yTop.toInt(), onArgb, shadow)

    val unitX = rightEdge + unitPadding
    drawString(font, Component.literal(unitText), unitX.toInt(), yTop.toInt(), onArgb, false)
}
