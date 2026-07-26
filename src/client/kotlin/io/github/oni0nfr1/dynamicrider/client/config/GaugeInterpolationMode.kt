package io.github.oni0nfr1.dynamicrider.client.config

enum class GaugeInterpolationMode(
    val key: String,
) {
    RAW("raw"),
    LINEAR_INTERPOLATION("linear_interpolation"),
    EXPONENTIAL_INTERPOLATION("exponential_interpolation"),
    LINEAR_EXTRAPOLATION("linear_extrapolation");

    val translationKey: String
        get() = "dynamicrider.config.gauge_interpolation.$key"

    val descriptionTranslationKey: String
        get() = "$translationKey.description"
}
