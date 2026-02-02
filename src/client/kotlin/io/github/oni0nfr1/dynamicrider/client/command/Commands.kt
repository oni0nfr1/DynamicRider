package io.github.oni0nfr1.dynamicrider.client.command

import com.mojang.brigadier.Command
import io.github.oni0nfr1.korigadier.api.Args
import io.github.oni0nfr1.korigadier.api.fragment
import io.github.oni0nfr1.korigadier.api.get
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object Commands {
    // 일반 엔진
    private val engines = listOf(
        "pro"    to 18,
        "new"    to 13,
        "jiu"    to 12,
        "x"      to 10,
        "v1"     to 15,
        "ex"     to 11,
        "1.0"    to 17,
        "z7"     to 14,
        "a2"     to 16,
        "rally"  to 19,
        "charge" to 20,
    )

    // 더미 엔진
    private val dummyEngines = listOf(
        "n1"     to 1000,
        "key"    to 1002,
        "mk"     to 1003,
        "boat"   to 1004,
        "gear"   to 1005,
        "f1"     to 1006,
        "krp"    to 1007,
    )

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