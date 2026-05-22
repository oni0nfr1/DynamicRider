package io.github.oni0nfr1.dynamicrider.client.rider.backend.inventory

import io.github.oni0nfr1.dynamicrider.client.rider.backend.RiderBackend
import net.minecraft.client.Minecraft
import net.minecraft.world.item.Items

object KartTeamBoostCounter: RiderBackend() {
    override fun init() { /* does nothing */ }

    val boostCount: Int
        get() {
            val mainHandItem = Minecraft.getInstance().player?.mainHandItem ?: return 0
            return if (mainHandItem.`is`(Items.SOUL_CAMPFIRE)) mainHandItem.count else 0
        }
}