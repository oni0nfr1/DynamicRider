package io.github.oni0nfr1.dynamicrider.client.animation

import kotlin.math.floor

class LoopTimer(
    val intervalMillis: Long,
    initialSpeed: Double = 1.0,
    private val nanoTime: () -> Long = System::nanoTime,
) : AnimationTimer {
    init {
        require(intervalMillis > 0L) {
            "intervalMillis must be greater than 0"
        }
    }

    private val intervalNanos = intervalMillis * 1_000_000L

    override var running = false
        private set
    private var recentNanos = 0L
    private var progressValue = 0.0

    var speed: Double = initialSpeed
        set(value) {
            updateProgress()
            field = value
        }

    override val progress: Float
        get() {
            updateProgress()
            return progressValue.toFloat()
        }

    override fun start() {
        if (running) {
            return
        }

        running = true
        recentNanos = nanoTime()
    }

    override fun stop() {
        updateProgress()
        running = false
    }

    fun restart() {
        progressValue = 0.0
        recentNanos = nanoTime()
        running = true
    }

    fun reset(progress: Double = 0.0) {
        progressValue = normalizeProgress(progress)
        recentNanos = nanoTime()
    }

    private fun updateProgress() {
        val currentNanos = nanoTime()

        if (!running) {
            recentNanos = currentNanos
            return
        }

        val elapsedNanos = currentNanos - recentNanos
        recentNanos = currentNanos

        val deltaProgress =
            elapsedNanos.toDouble() / intervalNanos.toDouble() * speed

        progressValue = normalizeProgress(progressValue + deltaProgress)
    }

    private fun normalizeProgress(value: Double): Double {
        return value - floor(value)
    }
}
