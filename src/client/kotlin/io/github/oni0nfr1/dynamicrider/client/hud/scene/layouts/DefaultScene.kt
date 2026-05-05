package io.github.oni0nfr1.dynamicrider.client.hud.scene.layouts

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBarBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.PlainNitroSlotBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.rankingtable.PlainRankingTableBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedmeter.JiuTachometerBuilder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.timer.HudTimerBuilder
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

fun defaultSceneDefinition() =
hudSceneDefinition<NitroEngine> {
    element<JiuTachometerBuilder> {
        layout {
            screenAnchor = HudAnchor.BOTTOM_CENTER
            elementAnchor = HudAnchor.BOTTOM_CENTER
        }
    }

    element<GradientGaugeBarBuilder> {
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

    element<PlainNitroSlotBuilder> {
        layout {
            screenAnchor = HudAnchor.TOP_LEFT
            elementAnchor = HudAnchor.TOP_LEFT
            x = 10
            y = 10
        }

        slotIndex = 1
        iconSize = 32
    }

    element<PlainNitroSlotBuilder> {
        layout {
            screenAnchor = HudAnchor.TOP_LEFT
            elementAnchor = HudAnchor.TOP_LEFT
            x = 62
            y = 10
        }

        slotIndex = 2
        iconSize = 28
    }

    element<PlainNitroSlotBuilder> {
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

    element<HudTimerBuilder> {
        layout {
            screenAnchor = HudAnchor.TOP_RIGHT
            elementAnchor = HudAnchor.TOP_RIGHT
            x = -10
            y = 10
        }

        minWidth = 100
    }

    element<PlainRankingTableBuilder> {
        layout {
            screenAnchor = HudAnchor.MIDDLE_LEFT
            elementAnchor = HudAnchor.MIDDLE_LEFT
            x = 10
        }
    }
}

fun defaultScene(kart: KartRef.Specific<NitroEngine>) =
    defaultSceneDefinition().create(kart)
