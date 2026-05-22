package io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.ride

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.NitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.rankingtable.PlainRankingTable
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.ChargeTachometer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.timer.HudTimer
import io.github.oni0nfr1.dynamicrider.client.hud.scene.hudScene
import io.github.oni0nfr1.skid.client.api.engine.ChargeEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

fun defaultChargeRideScene(kart: KartRef.Specific<ChargeEngine>) =
hudScene(kart) {
    element<ChargeTachometer.Builder> {
        layout {
            screenAnchor = HudAnchor.BOTTOM_CENTER
            elementAnchor = HudAnchor.BOTTOM_CENTER

            scaleX = 0.5f
            scaleY = 0.5f
        }
    }

    element<NitroSlot.Builder> {
        layout {
            screenAnchor = HudAnchor.TOP_LEFT
            elementAnchor = HudAnchor.TOP_LEFT
            x = 10
            y = 10

            scaleX = 0.5f
            scaleY = 0.5f
        }

        slotIndex = 1
        backgroundColor = 0xFFFF0000.toInt()
    }

    element<NitroSlot.Builder> {
        layout {
            screenAnchor = HudAnchor.TOP_LEFT
            elementAnchor = HudAnchor.TOP_LEFT
            x = 60
            y = 10

            scaleX = 0.375f
            scaleY = 0.375f
        }

        slotIndex = 2
        backgroundColor = 0xFFFF0000.toInt()
    }

    element<NitroSlot.Builder> {
        layout {
            screenAnchor = HudAnchor.TOP_LEFT
            elementAnchor = HudAnchor.TOP_LEFT
            x = 100
            y = 10

            scaleX = 0.375f
            scaleY = 0.375f
        }

        slotIndex = 3
        backgroundColor = 0xFFFF0000.toInt()
    }

    element<HudTimer.Builder> {
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
        }
    }
}
