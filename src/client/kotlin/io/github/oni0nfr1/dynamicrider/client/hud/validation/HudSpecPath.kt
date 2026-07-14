package io.github.oni0nfr1.dynamicrider.client.hud.validation

/** HUD spec 내부의 property 또는 collection element를 가리키는 구조화된 경로다. */
data class HudSpecPath(
    val segments: List<String> = emptyList(),
) {
    fun child(segment: String): HudSpecPath {
        require(segment.isNotBlank()) { "HUD spec path segment must not be blank" }
        return HudSpecPath(segments + segment)
    }

    override fun toString(): String = segments.joinToString(".").ifEmpty { "<root>" }
}
