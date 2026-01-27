package io.github.oni0nfr1.dynamicrider.client.command.debug

import java.lang.reflect.Field

data class RegisteredVar(
    val name: String,
    val owner: Any?,
    val field: Field,
    val isStatic: Boolean,
    internal val codec: VarCodec
)
