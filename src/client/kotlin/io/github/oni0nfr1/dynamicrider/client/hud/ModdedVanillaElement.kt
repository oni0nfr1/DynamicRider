package io.github.oni0nfr1.dynamicrider.client.hud

import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import org.joml.Vector2f
import org.joml.Vector3f

/**
 * # TODO
 *
 * 위치가 옮겨졌거나, 보여지는 방식이 바뀌는 마크 기존의 화면 요소
 * 경험치 바나 액션바 위치를 옮길 때 사용할 예정
 */
abstract class ModdedVanillaElement: HudElement {
    override var screenAnchor: HudAnchor
        get() = TODO("Not yet implemented")
        set(value) {}
    override var elementAnchor: HudAnchor
        get() = TODO("Not yet implemented")
        set(value) {}
    override var scale: Vector2f
        get() = TODO("Not yet implemented")
        set(value) {}
    override var position: Vector3f
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun draw(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        TODO("Not yet implemented")
    }
}