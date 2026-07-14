package io.github.oni0nfr1.dynamicrider.client.hud.elements.timer

import io.github.oni0nfr1.dynamicrider.client.graphics.util.textWithDynriderFont
import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HexColorSerdes
import io.github.oni0nfr1.dynamicrider.client.rider.time.Millis
import io.github.oni0nfr1.dynamicrider.client.rider.time.RaceTime
import io.github.oni0nfr1.dynamicrider.client.rider.backend.race.KartRaceTimer
import io.github.oni0nfr1.dynamicrider.client.util.milliseconds
import io.github.oni0nfr1.dynamicrider.client.util.minutes
import io.github.oni0nfr1.dynamicrider.client.util.seconds
import io.github.oni0nfr1.skid.client.api.attr.maxLap
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import kotlin.math.max

class SpectateHudTimer(
    spec: Spec,
    kart: KartRef.Specific<KartEngine>,
    parent: ElementHolder,
) : HudElementImpl<KartEngine>(spec.layout, kart, parent) {
    private companion object {
        const val PADDING_PX = 6
        const val LAP_SUFFIX_GAP_PX = 1
        const val AFTER_LAP_GAP_PX = 4

        const val LAP_SCALE = 2.2f
        const val LAP_SUFFIX_SCALE = 1.35f
    }

    var minWidth: Int = spec.minWidth
    var txtColor: Int = spec.txtColor

    var time: RaceTime = RaceTime()

    var currentLap: Int = 1
    var maxLap: Int? = null

    private var visible: Boolean = false

    private var measuredWidth: Int = 0
    private var measuredHeight: Int = 0

    override val width: Int
        get() = measuredWidth
    override val height: Int
        get() = measuredHeight

    override fun updateLayout() {
        syncState()
        if (!visible) {
            measuredWidth = 0
            measuredHeight = 0
            return
        }

        val fontRenderer = Minecraft.getInstance().font

        val lapMainText = currentLap.coerceAtLeast(0).toString()
        val lapSuffixText = maxLap?.let { " /${it.coerceAtLeast(0)}" }.orEmpty()

        val timeValueText = formatTime(time.interpolatedTotalMillis)
        val timeLabelText = "TIME / "

        val lapMainWidthPx = fontRenderer.width(lapMainText) * LAP_SCALE
        val lapSuffixWidthPx = fontRenderer.width(lapSuffixText) * LAP_SUFFIX_SCALE
        val lapLineWidthPx = lapMainWidthPx + LAP_SUFFIX_GAP_PX + lapSuffixWidthPx

        val timeLineWidthPx = fontRenderer.width(timeLabelText) + fontRenderer.width(timeValueText)
        val contentWidthPx = max(lapLineWidthPx.toInt(), timeLineWidthPx)

        val bigLapHeightPx = (fontRenderer.lineHeight * LAP_SCALE).toInt()
        val contentHeightPx = bigLapHeightPx + AFTER_LAP_GAP_PX + fontRenderer.lineHeight

        val finalWidthPx = max(minWidth, contentWidthPx + (PADDING_PX * 2))
        val finalHeightPx = contentHeightPx + (PADDING_PX * 2)

        measuredWidth = finalWidthPx
        measuredHeight = finalHeightPx
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker,
    ) {
        syncState()
        if (!visible) return

        val fontRenderer = Minecraft.getInstance().font
        val paddingLeftPx = PADDING_PX
        val paddingTopPx = PADDING_PX

        val lapMainText = currentLap.coerceAtLeast(0).toString()
        val lapSuffixText = maxLap?.let { " /${it.coerceAtLeast(0)}" } ?: "Lap"
        val timeValueText = formatTime(time.interpolatedTotalMillis)

        val labelColor = withSameAlpha(txtColor, 0x00B0B0B0)
        val valueColor = txtColor

        var cursorYpx = paddingTopPx

        val poseStack = guiGraphics.pose()
        val bigLapHeightPx = fontRenderer.lineHeight * LAP_SCALE
        val lapMainWidthPx = fontRenderer.width(lapMainText) * LAP_SCALE

        poseStack.pushPose()
        poseStack.translate(paddingLeftPx.toDouble(), cursorYpx.toDouble(), 0.0)
        poseStack.scale(LAP_SCALE, LAP_SCALE, 1.0f)
        guiGraphics.textWithDynriderFont(0, 0, valueColor, lapMainText, true)
        poseStack.popPose()

        val suffixXpx = paddingLeftPx + lapMainWidthPx.toInt() + LAP_SUFFIX_GAP_PX
        val suffixHeightPx = (fontRenderer.lineHeight * LAP_SUFFIX_SCALE).toInt()
        val suffixYpx = cursorYpx + bigLapHeightPx.toInt() - suffixHeightPx

        poseStack.pushPose()
        poseStack.translate(suffixXpx.toDouble(), suffixYpx.toDouble(), 0.0)
        poseStack.scale(LAP_SUFFIX_SCALE, LAP_SUFFIX_SCALE, 1.0f)
        guiGraphics.textWithDynriderFont(0, 0, valueColor, lapSuffixText, true)
        poseStack.popPose()

        cursorYpx += bigLapHeightPx.toInt() + AFTER_LAP_GAP_PX

        drawLabelAndValue(
            guiGraphics = guiGraphics,
            fontRenderer = fontRenderer,
            labelText = "TIME / ",
            valueText = timeValueText,
            xPx = paddingLeftPx,
            yPx = cursorYpx,
            labelColor = labelColor,
            valueColor = valueColor,
        )
    }

    private fun syncState() {
        val client = Minecraft.getInstance()
        visible = KartRaceTimer.isRacing
        time = KartRaceTimer.time
        currentLap = kart.access { engine?.currentLap } ?: 0
        maxLap = client.level?.maxLap
    }

    private fun drawLabelAndValue(
        guiGraphics: GuiGraphics,
        fontRenderer: Font,
        labelText: String,
        valueText: String,
        xPx: Int,
        yPx: Int,
        labelColor: Int,
        valueColor: Int,
    ) {
        guiGraphics.textWithDynriderFont(xPx, yPx, labelColor, labelText, true)
        val valueStartXpx = xPx + fontRenderer.width(labelText)
        guiGraphics.textWithDynriderFont(valueStartXpx, yPx, valueColor, valueText, true)
    }

    private fun formatTime(millis: Millis): String {
        val minutes = millis.minutes
        val seconds = millis.seconds
        val milliseconds = millis.milliseconds

        val minuteText = minutes.toString().padStart(2, '0')
        val secondText = seconds.toString().padStart(2, '0')
        val milliText = milliseconds.toString().padStart(3, '0')

        return "$minuteText:$secondText:$milliText"
    }

    private fun withSameAlpha(baseArgb: Int, rgb24: Int): Int {
        val alpha = (baseArgb ushr 24) and 0xFF
        return (alpha shl 24) or (rgb24 and 0x00FFFFFF)
    }

    @Serializable
    @SerialName("SPECTATE_TIMER")
    data class Spec(
        override val layout: HudLayoutSpec,
        val minWidth: Int = 100,
        @Serializable(with = HexColorSerdes::class)
        val txtColor: Int = 0xFFFFFFFF.toInt(),
    ) : HudElementSpec<SpectateHudTimer, KartEngine> {
        override fun requiredEngineClass(): Class<out KartEngine> = KartEngine::class.java

        override fun create(
            kart: KartRef.Specific<KartEngine>,
            parent: ElementHolder,
        ): SpectateHudTimer = SpectateHudTimer(this, kart, parent)
    }

}
