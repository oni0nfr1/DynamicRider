package io.github.oni0nfr1.dynamicrider.client.event.impl

import io.github.oni0nfr1.dynamicrider.client.event.util.ListenerExceptionPolicy

import net.minecraft.world.InteractionResult
import org.slf4j.Logger
import java.util.concurrent.CopyOnWriteArrayList

class RiderEvent<T : Any>(
    private val logger: Logger,
    private val exceptionPolicy: ListenerExceptionPolicy = ListenerExceptionPolicy.default,
    invokerFactory: (Iterable<T>, (T, () -> InteractionResult) -> InteractionResult) -> T,
) {
    private val listeners = CopyOnWriteArrayList<T>()

    private val _invoker: T = invokerFactory(listeners) { listener, block ->
        callSafely(listener, block)
    }

    fun invoker() = _invoker

    fun register(listener: T): AutoCloseable {
        listeners.add(listener)
        return AutoCloseable { listeners.remove(listener) }
    }

    private fun callSafely(listener: T, block: () -> InteractionResult): InteractionResult {
        return try {
            block()
        } catch (t: Throwable) {
            when (exceptionPolicy) {
                ListenerExceptionPolicy.THROW -> throw t
                ListenerExceptionPolicy.LOG_AND_PASS -> {
                    logger.error("Event listener failed: {}", listener.javaClass.name, t)
                    InteractionResult.PASS
                }
                ListenerExceptionPolicy.LOG_AND_FAIL -> {
                    logger.error("Event listener failed: {}", listener.javaClass.name, t)
                    InteractionResult.FAIL
                }
            }
        }
    }
}