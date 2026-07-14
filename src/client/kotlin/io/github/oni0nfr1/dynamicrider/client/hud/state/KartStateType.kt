package io.github.oni0nfr1.dynamicrider.client.hud.state

/** 장면 경로와 요소 호환성 검사에 사용하는 안정적인 HUD 상태 타입이다. */
data class KartStateType<S : KartState>(
    val id: String,
    val stateClass: Class<S>,
) {
    fun accepts(requiredStateClass: Class<out KartState>): Boolean =
        requiredStateClass.isAssignableFrom(stateClass)
}

object KartStateTypes {
    val X = KartStateType("x", NitroDraftKartState::class.java)
    val EX = KartStateType("ex", NitroDraftKartState::class.java)
    val JIU = KartStateType("jiu", JiuKartState::class.java)
    val NEW = KartStateType("new", NitroDraftKartState::class.java)
    val Z7 = KartStateType("z7", NitroDraftKartState::class.java)
    val V1 = KartStateType("v1", V1KartState::class.java)
    val A2 = KartStateType("a2", NitroDraftKartState::class.java)
    val LEGACY = KartStateType("legacy", NitroDraftKartState::class.java)
    val PRO = KartStateType("pro", NitroDraftKartState::class.java)
    val RUSHPLUS = KartStateType("rushplus", NitroDraftKartState::class.java)
    val CHARGE = KartStateType("charge", ChargeKartState::class.java)
    val SR = KartStateType("sr", NitroDraftKartState::class.java)
    val N1 = KartStateType("n1", NitroDraftKartState::class.java)
    val RX = KartStateType("rx", NitroDraftKartState::class.java)
    val KEY = KartStateType("key", NitroKartState::class.java)
    val GEAR = KartStateType("gear", DraftSpeedKartState::class.java)
    val F1 = KartStateType("f1", DraftSpeedKartState::class.java)
    val RALLY = KartStateType("rally", DraftSpeedKartState::class.java)
    val MK = KartStateType("mk", DraftKartState::class.java)
    val BOAT = KartStateType("boat", KartState::class.java)

    val entries: List<KartStateType<out KartState>> = listOf(
        X, EX, JIU, NEW, Z7, V1, A2, LEGACY, PRO, RUSHPLUS,
        CHARGE, SR, N1, RX, KEY, GEAR, F1, RALLY, MK, BOAT,
    )

    private val byId = entries.associateBy(KartStateType<*>::id)

    fun byId(id: String): KartStateType<out KartState>? = byId[id]
}
