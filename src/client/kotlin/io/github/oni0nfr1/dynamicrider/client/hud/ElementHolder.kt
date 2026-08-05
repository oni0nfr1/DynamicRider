package io.github.oni0nfr1.dynamicrider.client.hud

import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement

/** [ElementHolder]가 직접 보유한 runtime 요소와 부모 내부의 안정적인 local key다. */
data class HeldHudElement(
    val key: String,
    val element: HudElement<*>,
) {
    init {
        require(key.isNotBlank()) { "Held HUD element key must not be blank" }
    }
}

interface ElementHolder {
    val width: Int
    val height: Int

    /** 직접 보유한 runtime 요소의 읽기 전용 snapshot이다. */
    val heldElements: List<HeldHudElement>
        get() = emptyList()
}
