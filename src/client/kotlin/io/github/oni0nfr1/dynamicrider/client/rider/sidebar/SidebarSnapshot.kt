package io.github.oni0nfr1.dynamicrider.client.rider.sidebar

import net.minecraft.network.chat.Component
import net.minecraft.world.scores.Objective

data class SidebarSnapshot(
    val title: Component,          // 예: "00,44,142"
    val objective: Objective?,
    val lines: List<SidebarLine>,
)