package io.github.oni0nfr1.dynamicrider.client.hud.elements

import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager

class HiddenNitroSlot(
    manager: HudStateManager,
    composer: PlainNitroSlot.() -> Unit
): PlainNitroSlot(manager, composer) {
    init { hide = true }
}