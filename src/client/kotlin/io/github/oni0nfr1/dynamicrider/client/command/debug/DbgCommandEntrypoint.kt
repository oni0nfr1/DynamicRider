package io.github.oni0nfr1.dynamicrider.client.command.debug

import com.mojang.brigadier.Command
import io.github.oni0nfr1.korigadier.api.KRootScope

fun <S> KRootScope<S>.registerDbgVariables(
    registry: DebugVarRegistry,
    feedback: (S, String) -> Unit,
    error: (S, String) -> Unit = feedback,
    rootLiteral: String = "debugvalue",
) {
    literal(rootLiteral) {

        // /debugvalue list
        literal("list") {
            executes { ctx ->
                val source = ctx.source
                val all = registry.all()
                if (all.isEmpty()) {
                    feedback(source, "[$rootLiteral] (empty)")
                    return@executes Command.SINGLE_SUCCESS
                }

                // 너무 길어지면 여러 줄이 나을 수 있는데, 일단 간단히 한 줄+개수
                feedback(source, "[$rootLiteral] vars(${all.size}): " + all.joinToString { it.name })
                Command.SINGLE_SUCCESS
            }
        }

        // /debugvalue echo <variable>
        literal("echo") {
            for (v in registry.all()) {
                literal(v.name) {
                    executes { ctx ->
                        val source = ctx.source
                        feedback(source, "[$rootLiteral] ${v.name} = ${registry.formatValue(v)}")
                        Command.SINGLE_SUCCESS
                    }
                }
            }
        }

        // /debugvalue set <variable> <value>
        literal("set") {
            for (v in registry.all()) {
                literal(v.name) {
                    argument("value", v.codec.argumentType) {
                        executes { ctx ->
                            val source = ctx.source
                            try {
                                registry.setFromContext(v, ctx, "value")
                                feedback(source, "[$rootLiteral] set ${v.name} = ${registry.formatValue(v)}")
                                Command.SINGLE_SUCCESS
                            } catch (t: Throwable) {
                                error(source, "[$rootLiteral] failed to set ${v.name}: ${t::class.java.simpleName}: ${t.message}")
                                0
                            }
                        }
                    }
                }
            }
        }
    }
}