package io.github.oni0nfr1.dynamicrider.client.hud.editor.document

import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneSpec

/** 편집 중인 요소의 영속 ID와 직렬화 가능한 명세를 묶는다. */
data class HudDocumentElement(
    val id: String,
    val spec: HudElementSpec<*, *>,
)

/**
 * GUI 편집기가 변경하는 HUD 장면의 가변 작업 사본이다.
 *
 * 외부 변경은 [HudEditCommand][io.github.oni0nfr1.dynamicrider.client.hud.editor.command.HudEditCommand]를
 * 통해 수행하며, [elements]는 읽기 전용 snapshot으로 노출한다.
 */
class HudSceneDocument private constructor(
    private val mutableElements: MutableList<HudDocumentElement>,
) {
    val elements: List<HudDocumentElement>
        get() = mutableElements.toList()

    var dirty: Boolean = false
        private set

    /** 현재 작업 사본을 요소 ID가 포함된 저장용 장면 명세로 변환한다. */
    fun toSpec(): HudSceneSpec = HudSceneSpec(
        elementIds = mutableElements.map(HudDocumentElement::id),
        elements = mutableElements.map(HudDocumentElement::spec),
    )

    /** 현재 document가 저장된 상태와 일치한다고 표시한다. */
    fun markClean() {
        dirty = false
    }

    internal fun add(index: Int, element: HudDocumentElement) {
        require(mutableElements.none { it.id == element.id }) { "Duplicate HUD element id '${element.id}'" }
        mutableElements.add(index.coerceIn(0, mutableElements.size), element)
        dirty = true
    }

    internal fun remove(id: String): Pair<Int, HudDocumentElement> {
        val index = mutableElements.indexOfFirst { it.id == id }
        require(index >= 0) { "Unknown HUD element id '$id'" }
        val removed = mutableElements.removeAt(index)
        dirty = true
        return index to removed
    }

    internal fun move(id: String, targetIndex: Int): Int {
        val sourceIndex = mutableElements.indexOfFirst { it.id == id }
        require(sourceIndex >= 0) { "Unknown HUD element id '$id'" }
        val element = mutableElements.removeAt(sourceIndex)
        mutableElements.add(targetIndex.coerceIn(0, mutableElements.size), element)
        dirty = true
        return sourceIndex
    }

    internal fun replace(id: String, spec: HudElementSpec<*, *>): HudElementSpec<*, *> {
        val index = mutableElements.indexOfFirst { it.id == id }
        require(index >= 0) { "Unknown HUD element id '$id'" }
        val previous = mutableElements[index].spec
        mutableElements[index] = HudDocumentElement(id, spec)
        dirty = true
        return previous
    }

    companion object {
        /**
         * 저장된 장면 명세로 편집 document를 생성한다.
         *
         * 요소 ID가 없는 이전 JSON에는 순서 기반의 결정적인 ID를 부여한다.
         */
        fun from(spec: HudSceneSpec): HudSceneDocument {
            val ids = if (spec.elementIds.size == spec.elements.size) {
                spec.elementIds
            } else {
                spec.elements.indices.map { "element-${it + 1}" }
            }
            return HudSceneDocument(
                ids.zip(spec.elements) { id, elementSpec -> HudDocumentElement(id, elementSpec) }
                    .toMutableList()
            )
        }
    }
}
