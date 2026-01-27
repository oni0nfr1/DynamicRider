package io.github.oni0nfr1.dynamicrider.client.command

import io.github.oni0nfr1.dynamicrider.client.command.debug.CommandVar

object DebugVariables {
    @field:CommandVar("value1")
    var value1 = 0
    @field:CommandVar("icon_slot_top_width")
    var iconSlotTopWidth = 50
    @field:CommandVar("icon_slot_bottom_width")
    var iconSlotBottomWidth = 44
}