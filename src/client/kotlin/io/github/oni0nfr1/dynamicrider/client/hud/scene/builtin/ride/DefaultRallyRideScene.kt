package io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.ride

import io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.template.emptyRideScene
import io.github.oni0nfr1.skid.client.api.engine.RallyEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

fun defaultRallyRideScene(kart: KartRef.Specific<RallyEngine>) = emptyRideScene(kart)
