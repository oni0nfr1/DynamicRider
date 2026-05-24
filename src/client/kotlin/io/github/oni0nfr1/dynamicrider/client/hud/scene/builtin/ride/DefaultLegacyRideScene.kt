package io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.ride

import io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.template.defaultNitroRideScene
import io.github.oni0nfr1.skid.client.api.engine.LegacyEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

fun defaultLegacyRideScene(kart: KartRef.Specific<LegacyEngine>) = defaultNitroRideScene(kart)
