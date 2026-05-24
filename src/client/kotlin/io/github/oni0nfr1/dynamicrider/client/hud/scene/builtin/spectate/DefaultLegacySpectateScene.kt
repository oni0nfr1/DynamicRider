package io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.spectate

import io.github.oni0nfr1.dynamicrider.client.hud.scene.builtin.template.defaultNitroSpectateScene
import io.github.oni0nfr1.skid.client.api.engine.LegacyEngine
import io.github.oni0nfr1.skid.client.api.kart.KartRef

fun defaultLegacySpectateScene(kart: KartRef.Specific<LegacyEngine>) = defaultNitroSpectateScene(kart)
