package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.gaugebar.GaugeBarBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.gaugebar.GaugeBarSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.v2.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.v2.hudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.v2.mount
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics

object SomeObject {
    var valueToRead: Int = 0

    fun increase() {
        valueToRead += 1
    }
}

class SomeElementBuilder : HudElementBuilder<SomeElementSpec>() {
    var a = 0

    override fun build(layout: HudLayoutSpec): SomeElementSpec {
        return SomeElementSpec(
            layout = layout,
            a = a,
        )
    }
}

@kotlinx.serialization.Serializable
data class SomeElementSpec(
    override val layout: HudLayoutSpec,
    val a: Int,
) : HudElementSpec<SomeElement>() {
    override fun create(): SomeElement = SomeElement(this)
}

class SomeElement(
    spec: SomeElementSpec,
) : HudElementImpl(spec.layout) {
    val a: Int = spec.a
    val currentValue: Int by SomeObject::valueToRead

    private fun renderText(): String = "a=$a, value=$currentValue"

    override fun resolveSize() {
        val font = net.minecraft.client.Minecraft.getInstance().font
        val text = renderText()
        setSize(font.width(text), font.lineHeight)
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        val text = renderText()
        guiGraphics.drawString(
            net.minecraft.client.Minecraft.getInstance().font,
            text,
            0,
            0,
            0xFFFFFF,
        )
    }
}

fun someScene(): HudScene =
    hudScene {
        onEnable {
            SomeObject.increase()
        }

        element<GaugeBarBuilder, GaugeBarSpec> {
            layout {
                screenAnchor = HudAnchor.BOTTOM_CENTER
                elementAnchor = HudAnchor.BOTTOM_CENTER
                y = -75
            }

            width = 120
            thickness = 8
            padding = 2
            smoothing = 3.0
        }

        element<SomeElementBuilder, SomeElementSpec> {
            layout {
                screenAnchor = HudAnchor.BOTTOM_CENTER
                elementAnchor = HudAnchor.BOTTOM_CENTER
                y = -90
            }
            a = 10
        }
    }

fun someMountedScene(stateManager: HudStateManager) = someScene().mount(stateManager)
