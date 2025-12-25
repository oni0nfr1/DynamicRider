package io.github.oni0nfr1.dynamicrider.client.hud.interfaces

import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import org.joml.Vector2f
import org.joml.Vector3f

// HudElement의 구현 클래스는 인터페이스를 통한 다중 상속을 이용해서 다음과 같은 것도 만들 수 있음
// GaugeBar와 NitroSlot, SpdMeter를 동시에 구현하여 하나로 합쳐진 타코미터 요소
// RankingTable과 TeamScoreboard를 동시에 상속해 점수 현황을 알려주는 것까지 합쳐진 팀전 랭킹 테이블
interface HudElement {

    var screenAnchor: HudAnchor
    var elementAnchor: HudAnchor
    var scale: Vector2f
    var position: Vector3f

    fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker)
}
