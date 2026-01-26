package io.github.oni0nfr1.dynamicrider.client.rider.actionbar

import io.github.oni0nfr1.dynamicrider.client.event.RiderActionBarCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.state.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.state.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.rider.BoosterType
import io.github.oni0nfr1.dynamicrider.client.rider.RiderBackend
import java.lang.AutoCloseable

class KartNitroCounter(
    override val stateManager: HudStateManager
): RiderBackend, AutoCloseable {
    companion object {
        val nitroRegex = Regex("""NITRO\s*x\s*(\d+)""", RegexOption.IGNORE_CASE)
        val fusionRegex = Regex("""FUSION\s*x\s*(\d+)""", RegexOption.IGNORE_CASE)
        val gearRegex = Regex("""(\d+)\s*단""", RegexOption.IGNORE_CASE)
    }

    private val eventListener = RiderActionBarCallback.EVENT.register { _, raw ->
        updateNitro(raw)
        HandleResult.PASS
    }

    val nitro: MutableState<Int> = mutableStateOf(stateManager, 0)
    val gear:  MutableState<Int> = mutableStateOf(stateManager, 0)
    val boosterType = mutableStateOf(stateManager, BoosterType.NITRO)

    private fun updateNitro(raw: String) {
        tryParse(raw, nitroRegex) { match ->
            val readNitro = match.groupValues[1].toInt()
            boosterType.set(BoosterType.NITRO)
            nitro.set(readNitro)
        }
        tryParse(raw, fusionRegex) { match ->
            val readNitro = match.groupValues[1].toInt()
            boosterType.set(BoosterType.FUSION)
            nitro.set(readNitro)
        }
        tryParse(raw, gearRegex) { match ->
            val readGear = match.groupValues[1].toInt()
            boosterType.set(BoosterType.GEAR)
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

    override fun close() {
        eventListener.close()
    }
}