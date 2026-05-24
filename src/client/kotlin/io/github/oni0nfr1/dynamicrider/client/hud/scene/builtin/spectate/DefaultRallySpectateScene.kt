package io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.spectate

import io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.template.emptySpectateScene
import io.github.oni0nfr1.skid.client.api.engine.RallyEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

fun defaultRallySpectateScene(kart: KartRef.Specific<RallyEngine>) = emptySpectateScene(kart)
