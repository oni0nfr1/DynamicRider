package io.github.oni0nfr1.dynamicrider.client.hud.editor.property

/** HUD document와 immutable spec 내부의 위치를 나타내는 비어 있지 않은 경로다. */
@ConsistentCopyVisibility
data class HudPath private constructor(
    val segments: List<String>,
) {
    init {
        require(segments.isNotEmpty()) { "HUD path must not be empty" }
        require(segments.none(String::isBlank)) { "HUD path segments must not be blank" }
    }

    val firstSegment: String
        get() = segments.first()

    val isSingle: Boolean
        get() = segments.size == 1

    val parent: HudPath?
        get() = segments.dropLast(1)
            .takeIf(List<String>::isNotEmpty)
            ?.let(::HudPath)

    fun child(segment: String): HudPath = HudPath(segments + segment)

    fun append(path: HudPath): HudPath = HudPath(segments + path.segments)

    override fun toString(): String = segments.joinToString(".", transform = ::encodeSegment)

    companion object {
        /** 하나 이상의 경로 세그먼트로 경로를 생성한다. */
        fun of(vararg segments: String): HudPath = HudPath(segments.toList())

        /** 점으로 구분되고 escape된 HUD 경로를 해석한다. */
        fun parse(path: String): HudPath = HudPath(path.split('.').map(::decodeSegment))

        private fun encodeSegment(segment: String): String =
            segment.replace("~", "~0").replace(".", "~1")

        private fun decodeSegment(segment: String): String = buildString(segment.length) {
            var index = 0
            while (index < segment.length) {
                val character = segment[index++]
                if (character != '~') {
                    append(character)
                    continue
                }

                require(index < segment.length) { "HUD path segment has an incomplete escape sequence" }
                when (val escaped = segment[index++]) {
                    '0' -> append('~')
                    '1' -> append('.')
                    else -> throw IllegalArgumentException(
                        "HUD path segment has an unknown escape sequence '~$escaped'",
                    )
                }
            }
        }
    }
}
