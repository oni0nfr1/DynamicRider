package io.github.oni0nfr1.dynamicrider.client.rider

import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.ExampleScene
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.animal.Cod
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object KartDetector {

    val logger: Logger = LoggerFactory.getLogger("mcrider-kart-detector")

    var detectTriesLeft = 0
    var pendingVehicleId: Int? = null

    const val KART_ENGINE_CODE = "mcrider-saddle-common"

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            tick(client)
        }
    }

    var lastVehicleId: Int? = null
    @JvmStatic
    fun detectKart(vehicleId: Int) {
        logger.info("Starting detectKart")

        if (lastVehicleId == vehicleId) return
        pendingVehicleId = vehicleId

        detectTriesLeft = 10 // 10틱 동안 CustomName 기다리기
        // CustomName 동기화 패킷이 플레이어 탑승 패킷보다 늦게 올라오는 상황을 대응하기 위함
    }

    fun tick(client: Minecraft) {
        val vehicleId = pendingVehicleId    ?: return
        val level = client.level            ?: return

        val entity = level.getEntity(vehicleId)
        val kart = entity as? Cod
        val kartName = kart?.customName?.string
        logger.info("Detected Kart $vehicleId (CustomName: $kartName)")

        if (kartName == KART_ENGINE_CODE) {
            lastVehicleId = vehicleId
            pendingVehicleId = null
            detectTriesLeft = 0

            onKartMount()
            return
        } else {
            detectTriesLeft--
            if (detectTriesLeft <= 0) pendingVehicleId = null
        }
    }

    fun onKartMount() {
        val mod = DynamicRiderClient.instance

        KartSpeedMeasure.enabled = true
        KartNitroCounter.enabled = true
        mod.currentScene = ExampleScene(mod.stateManager)
    }

    fun onKartDismount() {
        val mod = DynamicRiderClient.instance

        KartSpeedMeasure.enabled = false
        KartNitroCounter.enabled = false
        mod.currentScene = null
    }

}