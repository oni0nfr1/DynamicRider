package io.github.oni0nfr1.dynamicrider.client.animation

interface AnimationTimer {
    val running: Boolean
    val progress: Float

    fun start()
    fun stop()
}