package io.github.oni0nfr1.dynamicrider.client.command

import net.minecraft.client.Minecraft

fun sendCommand(command: String) {
    val client = Minecraft.getInstance()
    val localPlayer = client.player ?: return
    localPlayer.connection.sendCommand(command)
}