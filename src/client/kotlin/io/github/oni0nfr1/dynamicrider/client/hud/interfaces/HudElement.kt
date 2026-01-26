package io.github.oni0nfr1.dynamicrider.client.hud.interfaces

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import org.joml.Vector2f
import org.joml.Vector2i

// HudElement의 구현 클래스는 인터페이스를 통한 다중 상속을 이용해서 다양한 구조를 가질 수 있음
interface HudElement {

    var screenAnchor: HudAnchor
    var elementAnchor: HudAnchor
    var scale: Vector2f
    var position: Vector2i
    var zIndex: Float

    fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker)
}
