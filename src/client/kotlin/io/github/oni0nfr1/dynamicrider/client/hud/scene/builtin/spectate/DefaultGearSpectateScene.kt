package io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.spectate

import io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.template.emptySpectateScene
import io.github.oni0nfr1.skid.client.api.engine.GearEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

fun defaultGearSpectateScene(kart: KartRef.Specific<GearEngine>) = emptySpectateScene(kart)
