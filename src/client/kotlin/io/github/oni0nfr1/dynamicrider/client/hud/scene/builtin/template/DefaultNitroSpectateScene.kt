package io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.template

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.PlainNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.rankingtable.PlainRankingTable
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedmeter.JiuStyleSpdMeter
import io.github.oni0nfr1.dynamicrider.client.hud.elements.timer.SpectateHudTimer
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.hudScene
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

inline fun <reified E> defaultNitroSpectateScene(kart: KartRef.Specific<E>): HudScene<E>
    where
        E : NitroEngine =
hudScene(kart) {
    element<JiuStyleSpdMeter.Builder> {
        layout {
            screenAnchor = HudAnchor.BOTTOM_CENTER
            elementAnchor = HudAnchor.BOTTOM_CENTER
        }
    }

    element<GradientGaugeBar.Builder> {
        layout {
            screenAnchor = HudAnchor.BOTTOM_CENTER
            elementAnchor = HudAnchor.BOTTOM_CENTER
            y = -75
        }

        width = 120
        thickness = 8
        padding = 2
    }

    element<PlainNitroSlot.Builder> {
        layout {
            screenAnchor = HudAnchor.TOP_LEFT
            elementAnchor = HudAnchor.TOP_LEFT
            x = 10
            y = 10
        }

        slotIndex = 1
        iconSize = 32
    }

    element<PlainNitroSlot.Builder> {
        layout {
            screenAnchor = HudAnchor.TOP_LEFT
            elementAnchor = HudAnchor.TOP_LEFT
            x = 62
            y = 10
        }

        slotIndex = 2
        iconSize = 28
    }

    element<PlainNitroSlot.Builder> {
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

    element<SpectateHudTimer.Builder> {
        layout {
            screenAnchor = HudAnchor.TOP_RIGHT
            elementAnchor = HudAnchor.TOP_RIGHT
            x = -10
            y = 10
        }

        minWidth = 100
    }

    element<PlainRankingTable.Builder> {
        layout {
            screenAnchor = HudAnchor.MIDDLE_LEFT
            elementAnchor = HudAnchor.MIDDLE_LEFT
            x = 10
            y = 0
        }
    }
}
