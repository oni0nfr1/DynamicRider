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
        this.type = mutableStateOf(KartNitroCounter.stateManager, BoosterType.NITRO)
    }

    var enabled = false
    val nitroRegex = Regex("""NITRO\s*x\s*(\d+)""", RegexOption.IGNORE_CASE)
    val fusionRegex = Regex("""FUSION\s*x\s*(\d+)""", RegexOption.IGNORE_CASE)
    val gearRegex = Regex("""(\d+)\s*단""", RegexOption.IGNORE_CASE)

    lateinit var nitro: MutableState<Int>
    lateinit var gear: MutableState<Int>
    lateinit var type: MutableState<BoosterType>

    @JvmStatic
    fun updateNitro(raw: String) {
        if (!enabled) return

        tryParse(raw, nitroRegex) { match ->
            val readNitro = match.groupValues[1].toInt()
            type.set(BoosterType.NITRO)
            nitro.set(readNitro)
        }
        tryParse(raw, fusionRegex) { match ->
            val readNitro = match.groupValues[1].toInt()
            type.set(BoosterType.FUSION)
            nitro.set(readNitro)
        }
        tryParse(raw, gearRegex) { match ->
            val readGear = match.groupValues[1].toInt()
            type.set(BoosterType.GEAR)
            gear.set(readGear)
        }
    }

    enum class BoosterType {
        NITRO,
        FUSION,
        GEAR,
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