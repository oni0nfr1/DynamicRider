package io.github.oni0nfr1.dynamicrider.client.hud.elements.debug

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudColor
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudElementInfo
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudLayout
import io.github.oni0nfr1.dynamicrider.client.hud.metadata.annotation.HudRange
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudSceneContext
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HexColorSerdes
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

/** Editor property widget와 긴 목록 scroll을 수동 검증하기 위한 단순 HUD 요소다. */
class EditorPropertyStressElement(
    spec: Spec,
    context: HudSceneContext<KartState>,
    parent: ElementHolder,
) : HudElementImpl<KartState>(spec.layout, context, parent) {
    private val enabled = spec.enabled
    private val label = spec.label
    private val style = spec.style
    private val boxWidth = spec.boxWidth
    private val boxHeight = spec.boxHeight
    private val boxColor = spec.boxColor
    private val textColor = spec.textColor

    override val width: Int get() = boxWidth
    override val height: Int get() = boxHeight

    override fun render(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        if (!enabled) return
        when (style) {
            StressStyle.SOLID -> guiGraphics.fill(0, 0, width, height, boxColor)
            StressStyle.OUTLINE -> guiGraphics.renderOutline(0, 0, width, height, boxColor)
            StressStyle.CHECKER -> {
                guiGraphics.fill(0, 0, width, height, boxColor)
                guiGraphics.fill(0, 0, width / 2, height / 2, boxColor xor 0x00202020)
                guiGraphics.fill(width / 2, height / 2, width, height, boxColor xor 0x00202020)
            }
        }
        guiGraphics.drawString(Minecraft.getInstance().font, label, 4, 4, textColor)
    }

    @Serializable
    enum class StressStyle {
        SOLID,
        OUTLINE,
        CHECKER,
    }

    @Serializable
    @SerialName("EDITOR_PROPERTY_STRESS_TEST")
    @HudElementInfo(category = "other")
    data class Spec(
        @HudLayout
        override val layout: HudLayoutSpec = HudLayoutSpec(x = 20, y = 20),
        val enabled: Boolean = true,
        val label: String = "Property stress test",
        val style: StressStyle = StressStyle.SOLID,
        @HudRange(min = 20.0, max = 400.0, step = 1.0)
        val boxWidth: Int = 160,
        @HudRange(min = 20.0, max = 200.0, step = 1.0)
        val boxHeight: Int = 32,
        @HudColor(alpha = true)
        @Serializable(with = HexColorSerdes::class)
        val boxColor: Int = 0xC0406080.toInt(),
        @HudColor(alpha = true)
        @Serializable(with = HexColorSerdes::class)
        val textColor: Int = 0xFFFFFFFF.toInt(),
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value01: Int = 1,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value02: Int = 2,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value03: Int = 3,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value04: Int = 4,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value05: Int = 5,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value06: Int = 6,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value07: Int = 7,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value08: Int = 8,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value09: Int = 9,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value10: Int = 10,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value11: Int = 11,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value12: Int = 12,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value13: Int = 13,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value14: Int = 14,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value15: Int = 15,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value16: Int = 16,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value17: Int = 17,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value18: Int = 18,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value19: Int = 19,
        @HudRange(min = 0.0, max = 100.0, step = 1.0)
        val value20: Int = 20,
    ) : HudElementSpec<EditorPropertyStressElement, KartState> {
        override fun requiredStateClass(): Class<out KartState> = KartState::class.java

        override fun create(
            context: HudSceneContext<KartState>,
            parent: ElementHolder,
        ): EditorPropertyStressElement = EditorPropertyStressElement(this, context, parent)
    }
}
