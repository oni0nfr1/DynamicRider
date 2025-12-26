package io.github.oni0nfr1.dynamicrider.client.hud.scenes

import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainSpeedMeter
import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainGaugeBar
import io.github.oni0nfr1.dynamicrider.client.hud.elements.PlainNitroSlot
import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import io.github.oni0nfr1.dynamicrider.client.rider.KartGaugeMeasure
import io.github.oni0nfr1.dynamicrider.client.rider.KartNitroCounter
import io.github.oni0nfr1.dynamicrider.client.rider.KartSpeedMeasure
import org.joml.Vector3f

class ExampleScene(
    override val stateManager: HudStateManager
): HudScene {
    val speedMeter: PlainSpeedMeter = PlainSpeedMeter(stateManager) {
        speed = KartSpeedMeasure.speed()
        screenAnchor = HudAnchor.BOTTOM_RIGHT
        elementAnchor = HudAnchor.BOTTOM_RIGHT
        position = Vector3f(-10f, -10f, 0f)
    }

    val nitroSlot1: PlainNitroSlot = PlainNitroSlot(stateManager) {
        occupied = KartNitroCounter.nitro() >= 1
        screenAnchor = HudAnchor.TOP_LEFT
        elementAnchor = HudAnchor.TOP_LEFT
        position = Vector3f(10f, 10f, 0f)
        iconSize = 32
    }

    val nitroSlot2: PlainNitroSlot = PlainNitroSlot(stateManager) {
        occupied = KartNitroCounter.nitro() >= 2
        screenAnchor = HudAnchor.TOP_LEFT
        elementAnchor = HudAnchor.TOP_LEFT
        position = Vector3f(62f, 10f, 0f)
        iconSize = 28
    }

    val gaugeBar: PlainGaugeBar = PlainGaugeBar(stateManager) {
        gauge = KartGaugeMeasure.gauge()
        screenAnchor = HudAnchor.BOTTOM_CENTER
        elementAnchor = HudAnchor.BOTTOM_CENTER
        position = Vector3f(0f, -75f, 0f)
    }

    override val elements: MutableSet<HudElement>
        = mutableSetOf(
            speedMeter,
            nitroSlot1,
            nitroSlot2,
            gaugeBar,
        )

    override fun enable() {
        VanillaSuppression.suppressVanillaKartState = true
    }

    override fun disable() {
        VanillaSuppression.suppressVanillaKartState = false
    }
}
