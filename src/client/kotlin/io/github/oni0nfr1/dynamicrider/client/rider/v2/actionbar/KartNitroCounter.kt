package io.github.oni0nfr1.dynamicrider.client.rider.v2.actionbar

import io.github.oni0nfr1.dynamicrider.client.event.RiderActionBarCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.rider.BoosterType

object KartNitroCounter {
    val nitroRegex = Regex("""NITRO\s*x\s*(\d+)""", RegexOption.IGNORE_CASE)
    val fusionRegex = Regex("""FUSION\s*x\s*(\d+)""", RegexOption.IGNORE_CASE)
    val gearRegex = Regex("""(\d+)\s*단""", RegexOption.IGNORE_CASE)

    private val eventListener = RiderActionBarCallback.EVENT.register { _, raw ->
        updateNitro(raw)
        HandleResult.PASS
    }

    var nitro: Int = 0
    var gear:  Int = 0
    var boosterType = BoosterType.NITRO

    private fun updateNitro(raw: String) {
        tryParse(raw, nitroRegex) { match ->
            val readNitro = match.groupValues[1].toInt()
            boosterType = BoosterType.NITRO
            nitro = readNitro
        }
        tryParse(raw, fusionRegex) { match ->
            val readNitro = match.groupValues[1].toInt()
            boosterType = BoosterType.FUSION
            nitro = readNitro
        }
        tryParse(raw, gearRegex) { match ->
            val readGear = match.groupValues[1].toInt()
            boosterType = BoosterType.GEAR
            gear = readGear
        }
    }

    private inline fun tryParse(
        raw: String,
        regex: Regex,
        predicate: (MatchResult) -> Unit
    ) {
        val matches = regex.find(raw)
        if (matches != null) predicate.invoke(matches)
    }
}