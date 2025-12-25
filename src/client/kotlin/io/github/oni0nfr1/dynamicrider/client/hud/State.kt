package io.github.oni0nfr1.dynamicrider.client.hud

// React의 State와 비슷한 개념
interface State<T> {
    val value: T

    operator fun invoke(): T
}