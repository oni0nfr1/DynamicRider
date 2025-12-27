package io.github.oni0nfr1.dynamicrider.client.rider

/**
 * 카트뿐만 아니라 어떤 엔티티든 플레이어 자신의 탑승 상태를 관리함
 */
object RiderMountState {

    @JvmField var wasPassenger: Boolean = false
    @JvmField var currentVehicleId: Int? = null

    @JvmStatic fun reset() {
        wasPassenger = false
        currentVehicleId = null
    }

    @JvmStatic fun markMounted(vehicleId: Int) {
        wasPassenger = true
        currentVehicleId = vehicleId
    }

}