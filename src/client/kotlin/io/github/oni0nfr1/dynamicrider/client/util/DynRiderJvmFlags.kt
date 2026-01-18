package io.github.oni0nfr1.dynamicrider.client.util

object DynRiderJvmFlags {
    private const val DEV_PROPERTY_KEY = "oni0nfr1.dynrider.dev"

    val devMode: Boolean by lazy {
        val rawValue = System.getProperty(DEV_PROPERTY_KEY) ?: return@lazy false
        when (rawValue.trim().lowercase()) {
            "true", "1", "yes", "y", "on" -> true
            else -> false
        }
    }
}