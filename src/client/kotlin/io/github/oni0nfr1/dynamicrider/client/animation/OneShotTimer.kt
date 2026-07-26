package io.github.oni0nfr1.dynamicrider.client.animation

class OneShotTimer(
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

    override var running: Boolean = false
        private set
    private var recentNanos = 0L
    private var progressValue = 0.0

    var speed: Double = initialSpeed
        set(value) {
            require(value >= 0.0) {
                "speed must be greater than or equal to 0"
            }

            updateProgress()
            field = value
        }

    override val progress: Float
        get() {
            updateProgress()
            return progressValue.toFloat()
        }

    override fun start() {
        progressValue = 0.0
        recentNanos = nanoTime()
        running = true
    }

    fun pause() {
        updateProgress()
        running = false
    }

    override fun stop() {
        progressValue = 0.0
        recentNanos = nanoTime()
        running = false
    }

    fun restart() {
        start()
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

        progressValue = coerceProgress(progressValue + deltaProgress)
        if (progressValue >= 1.0) {
            running = false
        }
    }

    private fun coerceProgress(value: Double): Double {
        return value.coerceIn(0.0, 1.0)
    }
}
