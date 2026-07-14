package io.github.oni0nfr1.dynamicrider.client.hud.editor.property

/** immutable HUD spec에서 변경할 직렬화 property의 경로다. */
@ConsistentCopyVisibility
data class HudPropertyPath private constructor(
    val segments: List<String>,
) {
    init {
        require(segments.isNotEmpty()) { "HUD property path must not be empty" }
        require(segments.none(String::isBlank)) { "HUD property path segments must not be blank" }
    }

    override fun toString(): String = segments.joinToString(".")

    companion object {
        /** 하나 이상의 직렬화 property 이름으로 경로를 생성한다. */
        fun of(vararg segments: String): HudPropertyPath = HudPropertyPath(segments.toList())

        /** 점으로 구분된 직렬화 property 경로를 해석한다. */
        fun parse(path: String): HudPropertyPath = HudPropertyPath(path.split('.'))
    }
}
