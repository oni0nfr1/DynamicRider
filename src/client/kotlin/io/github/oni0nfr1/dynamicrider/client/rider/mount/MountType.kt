package io.github.oni0nfr1.dynamicrider.client.rider.mount

import io.github.oni0nfr1.dynamicrider.client.rider.KartEngine

//TODO: 추후 sealed interface를 이용하여 KartEngine이 들어가게 만들기

sealed interface MountType {
    class NotMounted : MountType
    class Mounted(val engine: KartEngine?) : MountType
    class Spectator : MountType
}