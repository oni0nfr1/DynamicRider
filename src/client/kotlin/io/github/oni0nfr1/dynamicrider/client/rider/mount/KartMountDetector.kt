package io.github.oni0nfr1.dynamicrider.client.rider.mount

import io.github.oni0nfr1.dynamicrider.client.event.RiderMountCallback
import io.github.oni0nfr1.dynamicrider.client.event.RiderSpectateCallback
import io.github.oni0nfr1.dynamicrider.client.event.attribute.RiderAttrCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.state.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.state.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.rider.KartEngine
import io.github.oni0nfr1.dynamicrider.client.rider.RiderBackend
import io.github.oni0nfr1.dynamicrider.client.util.isKart
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import kotlin.collections.mutableSetOf

class KartMountDetector(
    override val stateManager: HudStateManager
) : RiderBackend, AutoCloseable {

    val mountedEntityIds = mutableStateOf(stateManager, mutableSetOf<Int>())
    val playerMountStatus = mutableStateOf(stateManager, MountType.NOT_MOUNTED)
    val currentEngine: MutableState<KartEngine?>

    private val passengersByKartId = mutableMapOf<Int, MutableSet<Int>>()

    init {
        val engineCode = RiderAttrCallback.KART_ENGINE_REAL.currentValue
        currentEngine = if (engineCode != null) {
            mutableStateOf(
                stateManager,
                KartEngine.getByCode(engineCode.toInt())
            )
        } else {
            mutableStateOf(stateManager, null)
        }
    }

    val mountListener = RiderMountCallback.EVENT.register { vehicle, passengers ->
        updateMount(vehicle, passengers)
        HandleResult.PASS
    }

    val spectateListener = RiderSpectateCallback.EVENT.register { player, target ->
        checkCamera(player, target)
        HandleResult.PASS
    }

    val engineListener = RiderAttrCallback.KART_ENGINE_REAL.register { value ->
        val engineCode = value.toInt() + 10
        val engine = KartEngine.getByCode(engineCode)
        currentEngine.set(engine)
        HandleResult.PASS
    }

    private fun updateMount(vehicle: Entity, passengers: Array<Entity?>) {
        val client = Minecraft.getInstance()
        val level = client.level ?: return
        val kartId = vehicle.id

        val newIds = passengers.asSequence()
            .filterNotNull()
            .map { it.id }
            .toMutableSet()

        val oldIds = passengersByKartId.put(kartId, newIds) ?: emptySet()

        val removedIds = oldIds.asSequence()
            .filter { it !in newIds }
            .toSet()

        mountedEntityIds.mutateIfChanged {
            for (passengerId in removedIds) {
                val passenger = level.getEntity(passengerId)
                if (passenger == null || passenger.vehicle?.isKart() != true)
                    remove(passengerId)
            }
            addAll(newIds)
        }

        if (newIds.isEmpty()) passengersByKartId.remove(kartId)

        val player = client.player
        val currentStatus = playerMountStatus.silentRead()
        val isMounted = player != null && mountedEntityIds.silentRead().contains(player.id)

        if (isMounted) playerMountStatus.set(MountType.MOUNTED)
        else if (currentStatus == MountType.MOUNTED) playerMountStatus.set(MountType.NOT_MOUNTED)
    }

    private fun checkCamera(player: Player, target: Entity) {
        val mounted = mountedEntityIds.silentRead().contains(target.id)

        if (mounted) {
            if (player === target) playerMountStatus.set(MountType.MOUNTED)
            else playerMountStatus.set(MountType.SPECTATOR)
        } else {
            playerMountStatus.set(MountType.NOT_MOUNTED)
        }
    }

    override fun close() {
        mountListener.close()
        spectateListener.close()
        engineListener.close()
    }
}