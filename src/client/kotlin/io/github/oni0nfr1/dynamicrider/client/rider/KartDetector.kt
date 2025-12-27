package io.github.oni0nfr1.dynamicrider.client.rider

import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient
import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression
import io.github.oni0nfr1.dynamicrider.client.hud.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.ExampleScene
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.animal.Cod

typealias Kart = Cod

object KartDetector {

    lateinit var stateManager: HudStateManager
    lateinit var boosterType: MutableState<BoosterType>
    lateinit var mountType: MutableState<MountType>

    var detectTriesLeft = 0

    const val KART_ENGINE_CODE = "mcrider-saddle-common"

    fun init(stateManager: HudStateManager) {
        this.stateManager = stateManager
        this.boosterType = mutableStateOf(stateManager, BoosterType.NITRO)
        this.mountType = mutableStateOf(stateManager, MountType.DISMOUNTED)

        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            tick()
        }

        ClientPlayConnectionEvents.JOIN.register { _, _, client ->
            reset()

            client.execute {
                bootstrap(client)
            }
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            reset()
            RiderMountState.reset() // 아래 2) 참고
        }
    }

    fun reset() {
        detectTriesLeft = 0
        currentKart = null
    }

    // init은 게임 시작 시에, bootstrap은 게임 접속 시에
    @JvmStatic
    fun bootstrap(client: Minecraft) {
        val subject = client.subject ?: return
        val vehicle = subject.vehicle ?: return
        detectKart(vehicle.id)
    }

    val Minecraft.subject: Entity?
        get() {
            val player = this.player ?: return null
            val cam = this.cameraEntity
            return if (player.isSpectator && cam != null && cam != player) cam else player
        }

    @JvmStatic var currentKart: Kart? = null
    @JvmStatic
    fun detectKart(vehicleId: Int) {
        val client = Minecraft.getInstance()
        val subject = client.subject ?: return
        val level = client.level ?: return

        val newKart = level.getEntity(vehicleId) as? Kart ?: return
        if (subject.vehicle != newKart) return
        if (currentKart?.id == newKart.id && detectTriesLeft > 0) return

        currentKart = newKart
        VanillaSuppression.suppressVanillaKartState = true
        detectTriesLeft = 10
    }

    @JvmStatic
    fun triggerDismounted() {
        reset()
        onKartDismount()
    }

    fun tick() {
        if (detectTriesLeft <= 0) return
        val kart = currentKart ?: return
        val kartName = kart.customName?.string

        if (kartName == KART_ENGINE_CODE) {
            detectTriesLeft = 0
            onKartMount()
            return
        }

        detectTriesLeft--
        if (detectTriesLeft <= 0) {
            VanillaSuppression.suppressVanillaKartState = false
            currentKart = null
        }
    }

    fun onKartMount() {
        val client = Minecraft.getInstance()
        val mod = DynamicRiderClient.instance
        val mount = when (client.player?.isSpectator) {
            true -> MountType.SPECTATOR
            else -> MountType.MOUNTED
        }

        mountType.set(mount)
        KartSpeedMeasure.enabled = true
        KartNitroCounter.enabled = true
        KartGaugeMeasure.enabled = true
        mod.currentScene = ExampleScene(mod.stateManager)
    }

    fun onKartDismount() {
        val mod = DynamicRiderClient.instance

        mountType.set(MountType.DISMOUNTED)
        KartSpeedMeasure.enabled = false
        KartNitroCounter.enabled = false
        KartGaugeMeasure.enabled = false
        mod.currentScene = null
    }

    enum class MountType {
        DISMOUNTED,
        MOUNTED,
        SPECTATOR
    }

}