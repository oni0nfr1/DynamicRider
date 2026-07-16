package io.github.oni0nfr1.dynamicrider.client.hud.scene

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder
import io.github.oni0nfr1.dynamicrider.client.hud.elements.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.elements.impl.spec.HudElementSpec
import io.github.oni0nfr1.dynamicrider.client.hud.state.KartState
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidationResult
import io.github.oni0nfr1.dynamicrider.client.hud.validation.HudSpecValidator
import io.github.oni0nfr1.dynamicrider.client.util.debugLog
import io.github.oni0nfr1.dynamicrider.client.util.warnLog
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

/** ID, Spec 및 선택적으로 생성된 runtime 요소를 묶은 HUD scene entry snapshot이다. */
data class HudSceneEntry<S : KartState>(
    val id: String,
    val spec: HudElementSpec<*, S>,
    val element: HudElement<S>?,
)

/** 검증된 Spec으로 scene이 보관할 runtime 요소를 생성한다. */
fun interface HudSceneElementFactory<S : KartState> {
    fun create(
        spec: HudElementSpec<*, S>,
        context: HudSceneContext<S>,
        parent: ElementHolder,
    ): HudElement<S>
}

class HudScene<S : KartState>(
    private val context: HudSceneContext<S>,
    private val viewport: ElementHolder?,
    private val elementFactory: HudSceneElementFactory<S>,
) : ElementHolder {
    constructor(context: HudSceneContext<S>) : this(context, null)

    constructor(context: HudSceneContext<S>, viewport: ElementHolder?) : this(
        context,
        viewport,
        HudSceneElementFactory { spec, sceneContext, parent -> spec.create(sceneContext, parent) },
    )

    private data class MutableEntry<S : KartState>(
        val id: String,
        var spec: HudElementSpec<*, S>,
        var element: HudElement<S>? = null,
    )

    private val mutableEntries = mutableListOf<MutableEntry<S>>()
    private val onEnableCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val onDisableCallbacks: MutableList<() -> Unit> = mutableListOf()
    private val reportedRenderFailures = mutableSetOf<String>()
    private var active: Boolean = false

    /** 현재 ID, 순서, Spec 및 runtime 요소의 읽기 전용 snapshot이다. */
    val entries: List<HudSceneEntry<S>>
        get() = mutableEntries.map { HudSceneEntry(it.id, it.spec, it.element) }

    val isActive: Boolean
        get() = active

    internal val diagnosticName: String
        get() = "${context.kartStateType.id}@${System.identityHashCode(this).toString(16)}"

    override val width: Int
        get() = viewport?.width ?: Minecraft.getInstance().window.guiScaledWidth
    override val height: Int
        get() = viewport?.height ?: Minecraft.getInstance().window.guiScaledHeight

    /** [id]와 [spec]으로 scene entry를 [index]에 추가한다. */
    fun addElement(
        id: String,
        spec: HudElementSpec<*, *>,
        index: Int = Int.MAX_VALUE,
    ): HudSceneMutationResult {
        if (mutableEntries.any { it.id == id }) return HudSceneMutationResult.DuplicateElementId(id)
        validateSpec(spec)?.let { return it }

        @Suppress("UNCHECKED_CAST")
        val typedSpec = spec as HudElementSpec<*, S>
        val element = if (active) elementFactory.create(typedSpec, context, this) else null
        val actualIndex = index.coerceIn(0, mutableEntries.size)
        mutableEntries.add(actualIndex, MutableEntry(id, typedSpec, element))
        return HudSceneMutationResult.Applied
    }

    /** [id]에 해당하는 scene entry를 제거한다. */
    fun removeElement(id: String): HudSceneMutationResult {
        val index = mutableEntries.indexOfFirst { it.id == id }
        if (index < 0) return HudSceneMutationResult.ElementNotFound(id)
        mutableEntries.removeAt(index)
        reportedRenderFailures.remove(id)
        return HudSceneMutationResult.Applied
    }

    /** [id]에 해당하는 entry를 [targetIndex]로 이동한다. */
    fun moveElement(id: String, targetIndex: Int): HudSceneMutationResult {
        val sourceIndex = mutableEntries.indexOfFirst { it.id == id }
        if (sourceIndex < 0) return HudSceneMutationResult.ElementNotFound(id)
        val entry = mutableEntries.removeAt(sourceIndex)
        mutableEntries.add(targetIndex.coerceIn(0, mutableEntries.size), entry)
        return HudSceneMutationResult.Applied
    }

    /** [id]를 유지하면서 Spec과 활성 runtime 요소를 교체한다. */
    fun replaceElement(id: String, spec: HudElementSpec<*, *>): HudSceneMutationResult {
        val index = mutableEntries.indexOfFirst { it.id == id }
        if (index < 0) return HudSceneMutationResult.ElementNotFound(id)
        validateSpec(spec)?.let { return it }

        @Suppress("UNCHECKED_CAST")
        val typedSpec = spec as HudElementSpec<*, S>
        val element = if (active) elementFactory.create(typedSpec, context, this) else null
        mutableEntries[index].spec = typedSpec
        mutableEntries[index].element = element
        reportedRenderFailures.remove(id)
        return HudSceneMutationResult.Applied
    }

    /** 모든 scene entry를 제거한다. */
    fun clearElements() {
        mutableEntries.clear()
        reportedRenderFailures.clear()
    }

    fun onEnable(block: () -> Unit) {
        onEnableCallbacks += block
    }

    fun onDisable(block: () -> Unit) {
        onDisableCallbacks += block
    }

    fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        mutableEntries.forEachIndexed { index, entry ->
            try {
                entry.element?.draw(guiGraphics, deltaTracker)
                reportedRenderFailures.remove(entry.id)
            } catch (cause: Throwable) {
                if (reportedRenderFailures.add(entry.id)) {
                    warnLog(
                        "Failed to render HUD element: scene=$diagnosticName, index=$index, " +
                            "id=${entry.id}, spec=${entry.spec::class.java.name}",
                        cause,
                    )
                }
                throw cause
            }
        }
    }

    internal fun enable() {
        if (active) return
        debugLog("Activating HUD scene: scene=$diagnosticName, elements=${mutableEntries.size}")
        onEnableCallbacks.forEachIndexed { index, callback ->
            try {
                callback()
            } catch (cause: Throwable) {
                warnLog(
                    "HUD scene enable callback failed: scene=$diagnosticName, callbackIndex=$index",
                    cause,
                )
                throw cause
            }
        }
        val created = mutableEntries.mapIndexed { index, entry ->
            debugLog(
                "Creating HUD element: scene=$diagnosticName, index=$index, " +
                    "id=${entry.id}, spec=${entry.spec::class.java.name}"
            )
            try {
                elementFactory.create(entry.spec, context, this)
            } catch (cause: Throwable) {
                warnLog(
                    "Failed to create HUD element: scene=$diagnosticName, index=$index, " +
                        "id=${entry.id}, spec=${entry.spec::class.java.name}",
                    cause,
                )
                throw cause
            }
        }
        mutableEntries.zip(created).forEach { (entry, element) -> entry.element = element }
        active = true
        debugLog("Activated HUD scene: scene=$diagnosticName, elements=${mutableEntries.size}")
    }

    internal fun disable() {
        if (!active) return
        onDisableCallbacks.forEachIndexed { index, callback ->
            try {
                callback()
            } catch (cause: Throwable) {
                warnLog(
                    "HUD scene disable callback failed: scene=$diagnosticName, callbackIndex=$index",
                    cause,
                )
                throw cause
            }
        }
        mutableEntries.forEach { it.element = null }
        reportedRenderFailures.clear()
        active = false
        debugLog("Disabled HUD scene: scene=$diagnosticName")
    }

    private fun validateSpec(spec: HudElementSpec<*, *>): HudSceneMutationResult? {
        val requiredStateClass = spec.requiredStateClass()
        val sceneStateClass = context.kartStateType.stateClass
        if (!requiredStateClass.isAssignableFrom(sceneStateClass)) {
            return HudSceneMutationResult.IncompatibleState(
                requiredStateClass = requiredStateClass,
                sceneStateClass = sceneStateClass,
                specType = spec::class.java.name,
            )
        }

        return when (val validation = HudSpecValidator.validate(spec)) {
            HudSpecValidationResult.Valid -> null
            is HudSpecValidationResult.Invalid -> HudSceneMutationResult.InvalidSpec(validation.errors)
        }
    }
}
