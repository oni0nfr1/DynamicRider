package io.github.oni0nfr1.dynamicrider.client.rider

import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager

interface RiderBackend {
    val stateManager: HudStateManager
}