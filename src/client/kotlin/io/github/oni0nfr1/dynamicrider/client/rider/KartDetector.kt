package io.github.oni0nfr1.dynamicrider.client.rider

import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient
import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.MutableState
import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression
import io.github.oni0nfr1.dynamicrider.client.hud.mutableStateOf
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.ExampleScene
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.animal.Cod

object KartDetector {

    lateinit var stateManager: HudStateManager
    lateinit var type: MutableState<BoosterType>

    var detectTriesLeft = 0
    var pendingVehicleId: Int? = null

    const val KART_ENGINE_CODE = "mcrider-saddle-common"

    fun init(stateManager: HudStateManager) {
        this.stateManager = stateManager
        this.type = mutableStateOf(stateManager, BoosterType.NITRO)

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            tick(client)
        }
    }

    var lastVehicleId: Int? = null
    @JvmStatic
    fun detectKart(vehicleId: Int) {
        // 플레이어가 타는 순간부터 customName을 인식하는 사이에 들어오는 액션바 취소
        VanillaSuppression.suppressVanillaKartState = true

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

        if (kartName == KART_ENGINE_CODE) {
            lastVehicleId = vehicleId
            pendingVehicleId = null
            detectTriesLeft = 0

            onKartMount()
            return
        } else {
            detectTriesLeft--
            if (detectTriesLeft <= 0) {
                // 카트에 탄 게 아님이 확인되면 그 뒤로는 액션바를 띄워 줌
                VanillaSuppression.suppressVanillaKartState = false
                pendingVehicleId = null
            }
        }
    }

    fun onKartMount() {
        val mod = DynamicRiderClient.instance

        KartSpeedMeasure.enabled = true
        KartNitroCounter.enabled = true
        KartGaugeMeasure.enabled = true
        mod.currentScene = ExampleScene(mod.stateManager)
    }

    fun onKartDismount() {
        val mod = DynamicRiderClient.instance

        KartSpeedMeasure.enabled = false
        KartNitroCounter.enabled = false
        KartGaugeMeasure.enabled = false
        mod.currentScene = null
    }

}