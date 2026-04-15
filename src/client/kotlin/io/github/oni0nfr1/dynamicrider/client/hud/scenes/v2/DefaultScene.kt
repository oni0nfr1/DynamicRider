package io.github.oni0nfr1.dynamicrider.client.hud.scenes.v2

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.gaugebar.GradientGaugeBarBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.gaugebar.GradientGaugeBarSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.nitroslot.PlainNitroSlotBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.nitroslot.PlainNitroSlotSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.rankingtable.PlainRankingTableBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.rankingtable.PlainRankingTableSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.speedmeter.JiuTachometerBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.speedmeter.JiuTachometerSpec
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.timer.HudTimerBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.timer.HudTimerSpec

fun defaultScene() = hudScene {
    element<JiuTachometerBuilder, JiuTachometerSpec> {
        layout {
            screenAnchor = HudAnchor.BOTTOM_CENTER
            elementAnchor = HudAnchor.BOTTOM_CENTER
        }
    }

    element<GradientGaugeBarBuilder, GradientGaugeBarSpec> {
        layout {
            screenAnchor = HudAnchor.BOTTOM_CENTER
            elementAnchor = HudAnchor.BOTTOM_CENTER
            y = -75
        }

        targetGaugeAlpha = 0x00
        width = 120
        thickness = 8
        padding = 2
        smoothing = 1.0
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

    element<PlainRankingTableBuilder, PlainRankingTableSpec> {
        layout {
            screenAnchor = HudAnchor.MIDDLE_LEFT
            elementAnchor = HudAnchor.MIDDLE_LEFT
            x = 10
            y = 0
        }
    }
}
