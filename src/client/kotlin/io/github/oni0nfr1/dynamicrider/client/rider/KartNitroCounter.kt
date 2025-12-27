package io.github.oni0nfr1.dynamicrider.client.rider

import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.mutableStateOf

object KartNitroCounter {

    lateinit var stateManager: HudStateManager

    fun init(stateManager: HudStateManager) {
        this.stateManager = stateManager
        this.nitro = mutableStateOf(KartNitroCounter.stateManager, 0)
        this.gear = mutableStateOf(KartNitroCounter.stateManager, 0)
    }

    var enabled = false
        set(value) {
            if (value == false) {
                nitro.set(0)
                gear.set(0)
                KartDetector.boosterType.set(BoosterType.NITRO)
            }
            field = value
        }
    val nitroRegex = Regex("""NITRO\s*x\s*(\d+)""", RegexOption.IGNORE_CASE)
    val fusionRegex = Regex("""FUSION\s*x\s*(\d+)""", RegexOption.IGNORE_CASE)
    val gearRegex = Regex("""(\d+)\s*단""", RegexOption.IGNORE_CASE)

    lateinit var nitro: MutableState<Int>
    lateinit var gear: MutableState<Int>

    @JvmStatic
    fun updateNitro(raw: String) {
        if (!enabled) return

        tryParse(raw, nitroRegex) { match ->
            val readNitro = match.groupValues[1].toInt()
            KartDetector.boosterType.set(BoosterType.NITRO)
            nitro.set(readNitro)
        }
        tryParse(raw, fusionRegex) { match ->
            val readNitro = match.groupValues[1].toInt()
            KartDetector.boosterType.set(BoosterType.FUSION)
            nitro.set(readNitro)
        }
        tryParse(raw, gearRegex) { match ->
            val readGear = match.groupValues[1].toInt()
            KartDetector.boosterType.set(BoosterType.GEAR)
            gear.set(readGear)
        }
    }

    private fun tryParse(
        raw: String,
        regex: Regex,
        predicate: (MatchResult) -> Unit
    ) {
        val matches = regex.find(raw)
        if (matches != null) predicate.invoke(matches)
    }

}