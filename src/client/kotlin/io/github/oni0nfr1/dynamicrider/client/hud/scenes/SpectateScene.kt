package io.github.oni0nfr1.dynamicrider.client.hud.scenes

import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient
import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.GradientGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainRankingTable
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainTimer
import io.github.oni0nfr1.dynamicrider.client.hud.elements.gaugebar.InterpolatedGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.speedmeter.JiuTachometer
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.GaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.NitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.RankingTable
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.SpeedMeter
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.Timer
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartGaugeTracker
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartNitroCounter
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartSpeedometer
import io.github.oni0nfr1.dynamicrider.client.rider.bossbar.KartTeamBoostTracker
import io.github.oni0nfr1.dynamicrider.client.rider.exp.KartExpProgressReader
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.RaceTimeParser
import io.github.oni0nfr1.dynamicrider.client.util.warnLog

class SpectateScene(
    override val stateManager: HudStateManager
): HudScene {
    val dynRider: DynamicRiderClient
        get() = DynamicRiderClient.instance

    val speedMeter: SpeedMeter = JiuTachometer(stateManager) {
        speed = speedometer.speed()
        screenAnchor = HudAnchor.BOTTOM_CENTER
        elementAnchor = HudAnchor.BOTTOM_CENTER
        position.x = 0
        position.y = 0

        glow = speed >= 100
    }

    val nitroSlot1: NitroSlot = PlainNitroSlot(stateManager) {
        occupied = nitroCounter.nitro() >= 1
        screenAnchor = HudAnchor.TOP_LEFT
        elementAnchor = HudAnchor.TOP_LEFT
        position.x = 10
        position.y = 10
        iconSize = 32
    }

    val nitroSlot2: NitroSlot = PlainNitroSlot(stateManager) {
        occupied = nitroCounter.nitro() >= 2
        screenAnchor = HudAnchor.TOP_LEFT
        elementAnchor = HudAnchor.TOP_LEFT
        position.x = 62
        position.y = 10
        iconSize = 28
    }

    val nitroSlot3: NitroSlot = PlainNitroSlot(stateManager, hide = true) {
        occupied = nitroCounter.nitro() >= 3
        screenAnchor = HudAnchor.TOP_LEFT
        elementAnchor = HudAnchor.TOP_LEFT
        position.x = 110
        position.y = 10

        iconSize = 28

        hide = hide && !occupied // 한번 보여지면 계속 유지됨
    }

    val gaugeBar: GaugeBar = GradientGaugeBar(stateManager) {
        gauge = gaugeTracker.gauge()
        screenAnchor = HudAnchor.BOTTOM_CENTER
        elementAnchor = HudAnchor.BOTTOM_CENTER
        position.x = 0
        position.y = -75
    }

    val teamBoostGauge: GaugeBar = InterpolatedGaugeBar(stateManager) {
        gaugeColor = 0xFF0000FF.toInt()
        targetGaugeColor = 0x00000000

        screenAnchor = HudAnchor.BOTTOM_CENTER
        elementAnchor = HudAnchor.BOTTOM_CENTER
        position.x = 0
        position.y = -68

        smoothing = 3.0
        width = 120
        thickness = 5
        padding = 0

        gauge = teamBoostTracker.gauge().toDouble()
    }

    val expGaugeBar: GaugeBar = InterpolatedGaugeBar(stateManager) {
        gaugeColor = 0xFF00C800.toInt()
        targetGaugeColor = 0x00000000

        screenAnchor = HudAnchor.BOTTOM_CENTER
        elementAnchor = HudAnchor.BOTTOM_CENTER
        position.x = 0
        position.y = -61

        smoothing = 3.0
        width = 120
        thickness = 5
        padding = 0

        gauge = expProgressReader.progress().toDouble()
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
        position.x = 10
        position.y = 0
    }

    val timer: Timer = PlainTimer(stateManager) {
        val raceSession = dynRider.raceSession
        hide = !raceTimeParser.isRacing() || raceSession == null
        try {
            if (!hide) {
                require(raceSession != null) {
                    "Timer element is not hidden but race session is null."
                }
                time = raceTimeParser.time()
            }
        } catch (exception: Exception) {
            warnLog(exception.toString())
        }

        screenAnchor = HudAnchor.TOP_RIGHT
        elementAnchor = HudAnchor.TOP_RIGHT
        position.x = -10
        position.y = 10
    }

    override val elements: MutableList<HudElement>
    = mutableListOf(
        speedMeter,
        nitroSlot1,
        nitroSlot2,
        nitroSlot3,
        gaugeBar,
        teamBoostGauge,
        expGaugeBar,
        rankingTable,
        timer
    )

    val gaugeTracker: KartGaugeTracker = KartGaugeTracker(stateManager)
    val teamBoostTracker: KartTeamBoostTracker = KartTeamBoostTracker(stateManager)
    val expProgressReader: KartExpProgressReader = KartExpProgressReader(stateManager)
    val nitroCounter: KartNitroCounter = KartNitroCounter(stateManager)
    val speedometer:  KartSpeedometer  = KartSpeedometer(stateManager)
    val raceTimeParser: RaceTimeParser = RaceTimeParser(stateManager)

    override fun enable() {

    }

    override fun disable() {
        gaugeTracker.close()
        teamBoostTracker.close()
        expProgressReader.close()
        nitroCounter.close()
        speedometer.close()
        raceTimeParser.close()
    }
}