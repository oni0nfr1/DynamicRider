package io.github.oni0nfr1.dynamicrider.client.rider.exp

import io.github.oni0nfr1.dynamicrider.client.event.RiderExpUpdateCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.state.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.state.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.rider.RiderBackend
import net.minecraft.client.Minecraft

class KartExpProgressReader(
    override val stateManager: HudStateManager
) : RiderBackend, AutoCloseable {

    val progress: MutableState<Float>
    val level: MutableState<Int>
    val total: MutableState<Int>

    init {
        val player = Minecraft.getInstance().player
            ?: throw IllegalStateException("Player is not initialized!")

        val progressValue = player.experienceProgress
        val levelValue = player.experienceLevel
        val totalExpValue = player.totalExperience

        progress = mutableStateOf(stateManager, progressValue)
        level = mutableStateOf(stateManager, levelValue)
        total = mutableStateOf(stateManager, totalExpValue)
    }

    private val expListener = RiderExpUpdateCallback.EVENT.register { progress, level, total ->
        this.progress.set(progress)
        this.level.set(level)
        this.total.set(total)
        HandleResult.PASS
    }

    override fun close() {
        expListener.close()
    }
}