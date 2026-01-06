package io.github.oni0nfr1.dynamicrider.client.event.util

import net.fabricmc.loader.api.FabricLoader

enum class ListenerExceptionPolicy {
    THROW,
    LOG_AND_PASS,
    LOG_AND_FAIL;

    companion object {
        val default: ListenerExceptionPolicy
            = if (FabricLoader.getInstance().isDevelopmentEnvironment) THROW
              else LOG_AND_PASS
    }

}