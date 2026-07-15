package io.github.oni0nfr1.dynamicrider.client.hud.scene.loader

import io.github.oni0nfr1.dynamicrider.client.hud.scene.model.HudSceneSpec
import kotlinx.serialization.SerializationException
import net.minecraft.resources.ResourceLocation

/** Repository가 현재 resource pack의 HUD 장면과 decode 오류를 조회하는 읽기 경계다. */
interface HudSceneResourceSource {
    fun get(id: ResourceLocation): HudSceneSpec?

    fun getLoadError(id: ResourceLocation): SerializationException?
}
