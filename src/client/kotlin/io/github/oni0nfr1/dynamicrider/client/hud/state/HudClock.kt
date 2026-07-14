package io.github.oni0nfr1.dynamicrider.client.hud.state

/** HUD 효과와 프리뷰 재생이 공유하는 시간 공급원이다. */
fun interface HudClock {
    fun currentTimeMillis(): Long
}
