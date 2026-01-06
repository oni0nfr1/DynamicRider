package io.github.oni0nfr1.dynamicrider.client.rider.actionbar

import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.rider.RiderBackend

class NitroCounter(
    override val stateManager: HudStateManager
): RiderBackend {
    companion object {
        val nitroRegex = Regex("""NITRO\s*x\s*(\d+)""", RegexOption.IGNORE_CASE)
        val fusionRegex = Regex("""FUSION\s*x\s*(\d+)""", RegexOption.IGNORE_CASE)
        val gearRegex = Regex("""(\d+)\s*단""", RegexOption.IGNORE_CASE)
    }

    val nitro: MutableState<Int> = mutableStateOf(stateManager, 0)
    val gear:  MutableState<Int> = mutableStateOf(stateManager, 0)

}