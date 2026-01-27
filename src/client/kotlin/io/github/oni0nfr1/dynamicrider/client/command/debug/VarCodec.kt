package io.github.oni0nfr1.dynamicrider.client.command.debug

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import java.lang.reflect.Field

data class VarCodec(
    val javaFieldType: Class<*>,
    val argumentType: ArgumentType<*>,
    val readAny: (CommandContext<*>, String) -> Any,
    val writeAny: (Field, Any?, Any) -> Unit,
    val formatAny: (Field, Any?) -> String = { field, owner -> field.get(owner).toString() },
)
