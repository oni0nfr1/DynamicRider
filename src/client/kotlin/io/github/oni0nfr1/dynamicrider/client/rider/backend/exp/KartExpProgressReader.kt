package io.github.oni0nfr1.dynamicrider.client.rider.backend.exp

import io.github.oni0nfr1.dynamicrider.client.rider.backend.RiderBackend
import net.minecraft.client.Minecraft

object KartExpProgressReader : RiderBackend() {
    override fun init() { /* does nothing */ }

    var progress: Float = currentPlayer?.experienceProgress ?: 0f
        get() = currentPlayer?.experienceProgress ?: 0f
        private set

    var level: Int = currentPlayer?.experienceLevel ?: 0
        get() = currentPlayer?.experienceLevel ?: 0
        private set

    var total: Int = currentPlayer?.totalExperience ?: 0
        get() = currentPlayer?.totalExperience ?: 0
        private set

    private val currentPlayer
        get() = Minecraft.getInstance().player
}
