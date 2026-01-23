package io.github.oni0nfr1.dynamicrider.client.event.util

import io.github.oni0nfr1.dynamicrider.client.util.DynRiderJvmFlags

enum class ListenerExceptionPolicy {
    THROW,
    LOG_AND_PASS,
    LOG_AND_FAIL;

    companion object {
        val default: ListenerExceptionPolicy
            = if (DynRiderJvmFlags.devMode) THROW
              else LOG_AND_PASS
    }

}