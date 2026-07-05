package io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.spectate

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.nitroslot.DynNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.rankingtable.PlainRankingTable
import io.github.oni0nfr1.dynamicrider.client.hud.elements.tachometer.V1Tachometer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.timer.SpectateHudTimer
import io.github.oni0nfr1.dynamicrider.client.hud.scene.hudScene
import io.github.oni0nfr1.skid.client.api.engine.V1Engine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

fun defaultV1SpectateScene(kart: KartRef.Specific<V1Engine>) =
    hudScene(kart) {
        element<V1Tachometer.Builder> {
            layout {
                screenAnchor = HudAnchor.BOTTOM_CENTER
                elementAnchor = HudAnchor.BOTTOM_CENTER
                y = -5

                scaleX = 0.5f
                scaleY = 0.5f
            }
        }

        element<DynNitroSlot.Builder> {
            layout {
                screenAnchor = HudAnchor.TOP_LEFT
                elementAnchor = HudAnchor.TOP_LEFT
                x = 10
                y = 10

                scaleX = 0.5f
                scaleY = 0.5f
            }

            slotIndex = 1
            backgroundColor = 0xFF0000FF.toInt()
        }

        element<DynNitroSlot.Builder> {
            layout {
                screenAnchor = HudAnchor.TOP_LEFT
                elementAnchor = HudAnchor.TOP_LEFT
                x = 60
                y = 10

                scaleX = 0.375f
                scaleY = 0.375f
            }

            slotIndex = 2
            backgroundColor = 0xFF0000FF.toInt()
        }

        element<DynNitroSlot.Builder> {
            layout {
                screenAnchor = HudAnchor.TOP_LEFT
                elementAnchor = HudAnchor.TOP_LEFT
                x = 100
                y = 10

                scaleX = 0.375f
                scaleY = 0.375f
            }

            slotIndex = 3
            backgroundColor = 0xFF0000FF.toInt()
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
            }
        }
    }
