package io.github.oni0nfr1.dynamicrider.client.command.debug

import com.mojang.brigadier.context.CommandContext
import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.korigadier.api.Args
import io.github.oni0nfr1.korigadier.api.get
import java.lang.reflect.Modifier

class DebugVarRegistry {

    private val logger = ResourceStore.logger

    private val codecs: List<VarCodec> = listOf(
        VarCodec(
            javaFieldType = Int::class.javaPrimitiveType!!,
            argumentType = Args.int(),
            readAny = { ctx, arg -> ctx.get<Int>(arg) },
            writeAny = { field, owner, value -> field.setInt(owner, value as Int) },
        ),
        VarCodec(
            javaFieldType = Long::class.javaPrimitiveType!!,
            argumentType = Args.long(),
            readAny = { ctx, arg -> ctx.get<Long>(arg) },
            writeAny = { field, owner, value -> field.set(owner, value as Long) },
        ),
        VarCodec(
            javaFieldType = Float::class.javaPrimitiveType!!,
            argumentType = Args.float(),
            readAny = { ctx, arg -> ctx.get<Float>(arg) },
            writeAny = { field, owner, value -> field.set(owner, value as Float) },
        ),
        VarCodec(
            javaFieldType = Double::class.javaPrimitiveType!!,
            argumentType = Args.double(),
            readAny = { ctx, arg -> ctx.get<Double>(arg) },
            writeAny = { field, owner, value -> field.set(owner, value as Double) },
        ),
        VarCodec(
            javaFieldType = Boolean::class.javaPrimitiveType!!,
            argumentType = Args.bool(),
            readAny = { ctx, arg -> ctx.get<Boolean>(arg) },
            writeAny = { field, owner, value -> field.setBoolean(owner, value as Boolean) },
        ),
        VarCodec(
            javaFieldType = String::class.java,
            argumentType = Args.greedy(),
            readAny = { ctx, arg -> ctx.get<String>(arg) },
            writeAny = { field, owner, value -> field.set(owner, value as String) },
            formatAny = { field, owner -> "\"${field.get(owner) as String}\"" },
        ),
    )

    private val varsByName: LinkedHashMap<String, RegisteredVar> = linkedMapOf()

    fun scan(owner: Any) {
        val clazz = owner.javaClass

        for (field in clazz.declaredFields) {
            val ann = field.getAnnotation(CommandVar::class.java) ?: continue

            val isStatic = Modifier.isStatic(field.modifiers)
            // Kotlin object의 INSTANCE/Companion 같은 정적 필드 방어
            if (isStatic && (field.isSynthetic || field.name == "INSTANCE" || field.name == "Companion")) {
                logger.warn(
                    "Skip @CommandVar '{}' because field looks like Kotlin singleton plumbing: {}#{}",
                    ann.name, clazz.name, field.name
                )
                continue
            }
            // val이면 backing field가 final일 가능성 높음
            if (Modifier.isFinal(field.modifiers)) {
                logger.warn("Skip @CommandVar '{}' because field is final(val): {}#{}", ann.name, clazz.name, field.name)
                continue
            }

            val codec = codecs.firstOrNull { it.javaFieldType == field.type }
            if (codec == null) {
                logger.warn(
                    "Skip @CommandVar '{}' because unsupported type: {} ({}#{})",
                    ann.name, field.type.name, clazz.name, field.name
                )
                continue
            }

            if (varsByName.containsKey(ann.name)) {
                logger.warn("Duplicate @CommandVar name '{}'. Keep first, skip {}#{}", ann.name, clazz.name, field.name)
                continue
            }

            field.isAccessible = true
            val targetOwner: Any? = if (isStatic) null else owner
            varsByName[ann.name] = RegisteredVar(
                name = ann.name,
                owner = targetOwner,
                field = field,
                isStatic = isStatic,
                codec = codec
            )
        }
    }

    fun all(): List<RegisteredVar> = varsByName.values.toList()

    fun getByName(name: String): RegisteredVar? = varsByName[name]

    fun formatValue(v: RegisteredVar): String = v.codec.formatAny(v.field, v.owner)

    fun setFromContext(v: RegisteredVar, ctx: CommandContext<*>, argName: String) {
        val newValue = v.codec.readAny(ctx, argName)
        v.codec.writeAny(v.field, v.owner, newValue)
    }
}
