package io.github.oni0nfr1.dynamicrider.client.hud.scenes

import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainSpeedMeter
import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainRankingTable
import io.github.oni0nfr1.dynamicrider.client.hud.elements.TimerWithLap
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.NitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.RankingTable
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.SpeedMeter
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.Timer
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartGaugeTracker
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartNitroCounter
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartSpeedometer
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.RaceTimeParser
import io.github.oni0nfr1.dynamicrider.client.util.milliseconds
import io.github.oni0nfr1.dynamicrider.client.util.minutes
import io.github.oni0nfr1.dynamicrider.client.util.seconds
import org.joml.Vector2i

class ExampleScene(
    override val stateManager: HudStateManager
): HudScene {
    val dynRider: DynamicRiderClient
        get() = DynamicRiderClient.instance

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
        val raceSession = dynRider.raceSession
        hide = raceSession?.rankingManager?.isTimeAttack?.invoke() ?: true
        if (raceSession != null) {
            ranking = raceSession.rankingManager.ranking()
            racers = raceSession.rankingManager.racers()
            eliminated = raceSession.rankingManager.eliminated()
            alive = raceSession.rankingManager.alive()
        }

        screenAnchor = HudAnchor.MIDDLE_LEFT
        elementAnchor = HudAnchor.MIDDLE_LEFT
        position = Vector2i(10, 0)
    }

    val timer: Timer = TimerWithLap(stateManager) {
        val raceSession = dynRider.raceSession
        hide = !raceTimeParser.isRacing() || raceSession == null
        if (!hide) {
            // hide가 false면 raceSession은 null이 아닐 수밖에 없음
            currentLap = raceSession!!.lapTimer.currentLap()
            maxLap = raceSession.lapTimer.maxLap()

            val bestTimeMillisRead = raceSession.lapTimer.bestTime()
            val bestTimeTotalMillis = if (bestTimeMillisRead == Long.MAX_VALUE) 0 else bestTimeMillisRead
            bestTimeMinutes = bestTimeTotalMillis.minutes
            bestTimeSeconds = bestTimeTotalMillis.seconds
            bestTimeMilliseconds = bestTimeTotalMillis.milliseconds

            val totalMillis = raceTimeParser.time().interpolatedTotalMillis
            minutes = totalMillis.minutes
            seconds = totalMillis.seconds
            milliseconds = totalMillis.milliseconds
        }

        screenAnchor = HudAnchor.TOP_RIGHT
        elementAnchor = HudAnchor.TOP_RIGHT
        position = Vector2i(-10, 10)
    }

    override val elements: MutableList<HudElement>
        = mutableListOf(
            speedMeter,
            nitroSlot1,
            nitroSlot2,
            gaugeBar,
            rankingTable,
            timer
        )

    val gaugeTracker: KartGaugeTracker = KartGaugeTracker(stateManager)
    val nitroCounter: KartNitroCounter = KartNitroCounter(stateManager)
    val speedometer:  KartSpeedometer  = KartSpeedometer(stateManager)
    val raceTimeParser: RaceTimeParser = RaceTimeParser(stateManager)

    override fun enable() {

    }

    override fun disable() {
        gaugeTracker.close()
        nitroCounter.close()
        raceTimeParser.close()
    }
}
