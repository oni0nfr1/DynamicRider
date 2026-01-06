package io.github.oni0nfr1.dynamicrider.client.rider.sidebar

import net.minecraft.network.chat.Component

data class SidebarLine(
    val rank: Int,                 // 화면에 보이는 순서 기준
    val owner: String,             // score holder id (보통 닉네임)
    val name: Component,           // 화면에 찍을 이름(팀 포맷 반영)
    val value: Int,                // 점수(정렬용)
)