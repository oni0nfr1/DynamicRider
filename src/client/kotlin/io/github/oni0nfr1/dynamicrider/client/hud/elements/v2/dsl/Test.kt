package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.gaugebar.GaugeBarBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.gaugebar.GaugeBarSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.impl.HudElementImpl
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.nitroslot.PlainNitroSlotBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.nitroslot.PlainNitroSlotSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.spec.HudLayoutSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.speedmeter.JiuTachometerBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.speedmeter.JiuTachometerSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.timer.HudTimerBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.timer.HudTimerSpec
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

        element<JiuTachometerBuilder, JiuTachometerSpec> {
            layout {
                screenAnchor = HudAnchor.BOTTOM_CENTER
                elementAnchor = HudAnchor.BOTTOM_CENTER
            }
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

        element<PlainNitroSlotBuilder, PlainNitroSlotSpec> {
            layout {
                screenAnchor = HudAnchor.TOP_LEFT
                elementAnchor = HudAnchor.TOP_LEFT
                x = 10
                y = 10
            }

            slotIndex = 1
            iconSize = 32
        }

        element<PlainNitroSlotBuilder, PlainNitroSlotSpec> {
            layout {
                screenAnchor = HudAnchor.TOP_LEFT
                elementAnchor = HudAnchor.TOP_LEFT
                x = 62
                y = 10
            }

            slotIndex = 2
            iconSize = 28
        }

        element<PlainNitroSlotBuilder, PlainNitroSlotSpec> {
            layout {
                screenAnchor = HudAnchor.TOP_LEFT
                elementAnchor = HudAnchor.TOP_LEFT
                x = 110
                y = 10
            }

            slotIndex = 3
            iconSize = 28
            hideUntilOccupied = true
            keepVisibleAfterOccupied = true
        }

        element<HudTimerBuilder, HudTimerSpec> {
            layout {
                screenAnchor = HudAnchor.TOP_RIGHT
                elementAnchor = HudAnchor.TOP_RIGHT
                x = -10
                y = 10
            }

            minWidth = 100
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
