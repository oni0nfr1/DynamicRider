package io.github.oni0nfr1.dynamicrider.client.hud

class MutableState<T>(
    private val runtime: HudStateManager,
    initial: T
) : State<T> {

    private var _value: T = initial

    override val value: T
        get() {
            runtime.recordRead(this)
            return _value
        }

    fun set(newValue: T) {
        if (_value == newValue) return
        _value = newValue
        runtime.invalidateByState(this)
    }

    override operator fun invoke(): T = value
}

fun <T> mutableStateOf(runtime: HudStateManager, initial: T) = MutableState(runtime, initial)