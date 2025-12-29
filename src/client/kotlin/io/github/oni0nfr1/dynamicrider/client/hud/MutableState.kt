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

    /** 내부를 변경하는 용도: 컬렉션/가변 객체에만 쓰는 걸 권장 */
    fun mutate(action: T.() -> Unit) {
        _value.action()
        runtime.invalidateByState(this)
    }

    /** [action]이 true를 반환해야 invalidate됨 */
    fun mutateIfChanged(action: T.() -> Boolean) {
        val changed = _value.action()
        if (changed) runtime.invalidateByState(this)
    }

    override operator fun invoke(): T = value
}

fun <T> mutableStateOf(runtime: HudStateManager, initial: T) = MutableState(runtime, initial)