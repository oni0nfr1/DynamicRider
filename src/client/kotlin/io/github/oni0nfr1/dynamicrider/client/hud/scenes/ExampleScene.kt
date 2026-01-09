package io.github.oni0nfr1.dynamicrider.client.hud.scenes

import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainSpeedMeter
import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainRankingTable
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.NitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.RankingTable
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.SpeedMeter
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartGaugeTracker
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartNitroCounter
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartSpeedometer
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.KartRankingManager
import org.joml.Vector2i

class ExampleScene(
    override val stateManager: HudStateManager
): HudScene {
    val speedMeter: SpeedMeter = PlainSpeedMeter(stateManager) {
        speed = speedometer.speed()
        screenAnchor = HudAnchor.BOTTOM_RIGHT
        elementAnchor = HudAnchor.BOTTOM_RIGHT
        position = Vector2i(-10, -10)
    }

    val nitroSlot1: NitroSlot = PlainNitroSlot(stateManager) {
        occupied = nitroCounter.nitro() >= 1
        screenAnchor = HudAnchor.TOP_LEFT
        elementAnchor = HudAnchor.TOP_LEFT
        position = Vector2i(10, 10)
        iconSize = 32
    }

    val nitroSlot2: NitroSlot = PlainNitroSlot(stateManager) {
        occupied = nitroCounter.nitro() >= 2
        screenAnchor = HudAnchor.TOP_LEFT
        elementAnchor = HudAnchor.TOP_LEFT
        position = Vector2i(62, 10)
        iconSize = 28
    }

    val gaugeBar: GaugeBar = PlainGaugeBar(stateManager) {
        gauge = gaugeTracker.gauge()
        screenAnchor = HudAnchor.BOTTOM_CENTER
        elementAnchor = HudAnchor.BOTTOM_CENTER
        position = Vector2i(0, -75)
    }

    val rankingTable: RankingTable = PlainRankingTable(stateManager) {
        hide = rankingManager.isTimeAttack()
        ranking = rankingManager.ranking()
        racers = rankingManager.racers()
        eliminated = rankingManager.eliminated()
        alive = rankingManager.alive()

        screenAnchor = HudAnchor.MIDDLE_LEFT
        elementAnchor = HudAnchor.MIDDLE_LEFT
        position = Vector2i(10, 0)
    }

    override val elements: MutableList<HudElement>
        = mutableListOf(
            speedMeter,
            nitroSlot1,
            nitroSlot2,
            gaugeBar,
            rankingTable
        )

    val gaugeTracker: KartGaugeTracker = KartGaugeTracker(stateManager)
    val nitroCounter: KartNitroCounter = KartNitroCounter(stateManager)
    val speedometer:  KartSpeedometer  = KartSpeedometer(stateManager)
    val rankingManager: KartRankingManager = KartRankingManager(stateManager)

    override fun enable() {
        VanillaSuppression.suppressVanillaKartState = true
    }

    override fun disable() {
        VanillaSuppression.suppressVanillaKartState = false
        gaugeTracker.close()
        nitroCounter.close()
        rankingManager.close()
    }
}
