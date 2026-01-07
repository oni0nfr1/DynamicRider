package io.github.oni0nfr1.dynamicrider.client.hud.state

import java.util.Collections
import java.util.IdentityHashMap

class HudStateManager {

    private var currentElement: Any? = null

    // state -> dependents(elements)
    private val stateDeps = IdentityHashMap<State<*>, MutableSet<Any>>()

    // element -> usedStates (cleanup용)
    private val elementDeps = IdentityHashMap<Any, MutableSet<State<*>>>()

    // dirty set
    private val dirty = Collections.newSetFromMap(IdentityHashMap<Any, Boolean>())

    fun invalidate(element: Any) {
        dirty.add(element)
    }

    fun invalidateByState(state: State<*>) {
        stateDeps[state]?.forEach { dirty.add(it) }
    }

    internal fun recordRead(state: State<*>) {
        val el = currentElement ?: return
        stateDeps.getOrPut(state) { Collections.newSetFromMap(IdentityHashMap()) }.add(el)
        elementDeps.getOrPut(el) { Collections.newSetFromMap(IdentityHashMap()) }.add(state)
    }

    private fun clearDeps(element: Any) {
        val used = elementDeps.remove(element) ?: return
        for (s in used) stateDeps[s]?.remove(element)
    }

    fun recomposeIfDirty(element: Any, block: () -> Unit) {
        val need = dirty.remove(element) || !elementDeps.containsKey(element) // 최초 1회 포함
        if (!need) return

        clearDeps(element)
        val prev = currentElement
        currentElement = element
        try {
            block()
        } finally {
            currentElement = prev
        }
    }
}