package io.github.oni0nfr1.dynamicrider.client.hud.editor.document

import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.scene.custom.HudSceneSpec

data class HudDocumentElement(
    val id: String,
    val spec: HudElementSpec<*, *>,
)

class HudSceneDocument private constructor(
    private val mutableElements: MutableList<HudDocumentElement>,
) {
    val elements: List<HudDocumentElement>
        get() = mutableElements.toList()

    var dirty: Boolean = false
        private set

    fun toSpec(): HudSceneSpec = HudSceneSpec(
        elementIds = mutableElements.map(HudDocumentElement::id),
        elements = mutableElements.map(HudDocumentElement::spec),
    )

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
