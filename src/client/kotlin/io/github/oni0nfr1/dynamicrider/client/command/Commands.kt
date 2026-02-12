package io.github.oni0nfr1.dynamicrider.client.command

import com.mojang.brigadier.Command
import io.github.oni0nfr1.dynamicrider.client.rider.KartEngine
import io.github.oni0nfr1.korigadier.api.Args
import io.github.oni0nfr1.korigadier.api.fragment
import io.github.oni0nfr1.korigadier.api.get
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object Commands {

    private val engines = KartEngine.entries
        .filter { !it.isDummy }
        .map { it.engineName to it.engineCode }

    private val dummyEngines = KartEngine.entries
        .filter { it.isDummy }
        .map { it.engineName to it.engineCode }

    val setEngineCommand = fragment<FabricClientCommandSource> {
        literal("engine") {
            argument("engine-number", Args.int()) {
                executes { ctx ->
                    val engineNumber = ctx.get<Int>("engine-number")
                    sendCommand("trigger setengine set $engineNumber")
                    Command.SINGLE_SUCCESS
                }
            }

            engines.forEach { engine ->
                literal(engine.first) {
                    executes {
                        sendCommand("trigger setengine set ${engine.second}")
                        Command.SINGLE_SUCCESS
                    }
                }
            }
        }

        literal("dengine") {
            argument("engine-number", Args.int()) {
                executes { ctx ->
                    val engineNumber = ctx.get<Int>("engine-number")
                    sendCommand("trigger setengine set $engineNumber")
                    Command.SINGLE_SUCCESS
                }
            }

            dummyEngines.forEach { engine ->
                literal(engine.first) {
                    executes {
                        sendCommand("trigger setengine set ${engine.second}")
                        Command.SINGLE_SUCCESS
                    }
                }
            }
        }
    }

}