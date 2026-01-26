package io.github.oni0nfr1.dynamicrider.client.graphics

import io.github.oni0nfr1.dynamicrider.client.hud.scenes.HudScene
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper

// TODO
fun LayeredDrawerWrapper.registerScene(scene: HudScene) {
    // HudScene 안에 layers 프로퍼티를 널고 그 레이어들을 여기 호출
    // HudScene을 여러 레이어로 나눈 다음 자유롭게 조작하기 위함
    // 기존의 draw 함수는 제거될 가능성  높음
    // 추후에 DSL을 구성하게 될 가능성도 있으려나?
}