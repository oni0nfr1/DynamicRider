package io.github.oni0nfr1.dynamicrider.client.rider.backend

import io.github.oni0nfr1.skid.client.api.kart.KartRef
import io.github.oni0nfr1.skid.client.api.kart.KartSaddle
import net.minecraft.world.entity.Entity

abstract class RiderBackend {
    open fun onRiderMount(kart: KartRef, rider: Entity) {}
    open fun onRiderDismount(kartEntity: KartSaddle, rider: Entity) {}

    open fun onRaceStart() {}
    open fun onRaceEnd() {}

    private var initialized = false
    fun bootstrap() {
        initialized = true
        init()
    }

    protected abstract fun init()
}
