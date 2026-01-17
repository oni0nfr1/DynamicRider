package io.github.oni0nfr1.dynamicrider.client.util.schedule

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

object Ticker {

    interface TaskHandle {
        /** @return true if this call cancelled it, false if it was already cancelled */
        fun cancel(): Boolean
        val isCancelled: Boolean
    }

    private data class Task(
        var delay: Int,
        val period: Int?,
        val action: () -> Unit,
        private val cancelled: AtomicBoolean = AtomicBoolean(false)
    ) : TaskHandle {
        override fun cancel(): Boolean = cancelled.compareAndSet(false, true)
        override val isCancelled: Boolean get() = cancelled.get()
    }

    private val pendingAdd = ConcurrentLinkedQueue<Task>()
    private val tasks = mutableListOf<Task>()
    private val initialized = AtomicBoolean(false)

    fun init() {
        if (!initialized.compareAndSet(false, true)) return
        ClientTickEvents.END_CLIENT_TICK.register { tick() }
    }

    /**
     * Cancels and removes all queued/running tasks (running action is not interrupted).
     * Call this on world change / disconnect for safety.
     */
    fun clear() {
        // cancel everything so external handles reflect cancellation
        for (t in tasks) t.cancel()
        while (true) {
            val t = pendingAdd.poll() ?: break
            t.cancel()
        }
        tasks.clear()
    }

    /**
     * Schedule one-shot task after [delay] ticks.
     * delay=0 means "run on this tick end" (END_CLIENT_TICK).
     */
    fun runTaskLater(delay: Int, action: () -> Unit): TaskHandle {
        val task = Task(delay.coerceAtLeast(0), null, action)
        pendingAdd.add(task)
        return task
    }

    /**
     * Schedule repeating task.
     * - First run after [delay] ticks.
     * - Next runs every [period] ticks. period=1 means "every tick".
     */
    fun runTaskTimer(delay: Int, period: Int, action: () -> Unit): TaskHandle {
        val task = Task(delay.coerceAtLeast(0), period.coerceAtLeast(1), action)
        pendingAdd.add(task)
        return task
    }

    private fun tick() {
        // drain pending queue
        while (true) {
            val t = pendingAdd.poll() ?: break
            if (!t.isCancelled) tasks.add(t)
        }
        if (tasks.isEmpty()) return

        // reverse loop to allow removeAt(i) safely without extra garbage
        var i = tasks.size - 1
        while (i >= 0) {
            val task = tasks[i]

            if (task.isCancelled) {
                tasks.removeAt(i)
                i--
                continue
            }

            if (task.delay > 0) {
                task.delay--
                i--
                continue
            }

            try {
                task.action()
            } catch (t: Throwable) {
                ResourceStore.logger.error("[DynamicRider] Ticker task failed", t)
            }

            val p = task.period
            if (p == null) {
                tasks.removeAt(i)
            } else {
                // period=1 => run every tick
                task.delay = p - 1
            }

            i--
        }
    }
}