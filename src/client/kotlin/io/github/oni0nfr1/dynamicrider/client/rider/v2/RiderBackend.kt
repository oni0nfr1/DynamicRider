package io.github.oni0nfr1.dynamicrider.client.rider.v2

import io.github.oni0nfr1.dynamicrider.client.event.Kart
import net.minecraft.world.entity.Entity

abstract class RiderBackend {
    open fun onRiderMount(kartEntity: Kart, rider: Entity) {}
    open fun onRiderDismount(kartEntity: Kart, rider: Entity) {}

    open fun onRaceStart() {}
    open fun onRaceEnd() {}

    private var initialized = false
    fun bootstrap() {
        initialized = true
        init()
    }

    protected abstract fun init()
}