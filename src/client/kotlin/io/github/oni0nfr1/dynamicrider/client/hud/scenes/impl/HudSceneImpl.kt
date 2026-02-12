package io.github.oni0nfr1.dynamicrider.client.hud.scenes.impl

import io.github.oni0nfr1.dynamicrider.client.hud.interfaces.HudElement
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.util.debugLog
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics

/**
 * HudScene의 개선판.
 *
 * 비활성화 시 이벤트 처리 모듈들을 자동으로 안전하게 정리해 주며,
 * elementMap 기반으로 개선되어 HUD 디자인 직렬화 기능으로의 확장성이 준비되었습니다.
 *
 * 추후 HudScene 인터페이스는 제거될 예정입니다.
 */
abstract class HudSceneImpl(
    override val stateManager: HudStateManager
): HudScene {

    val backendModules = BackendModules()

    @Deprecated("HudSceneImpl is deprecated. Instead use elementMap.", replaceWith = ReplaceWith("elementMap"))
    final override val elements: Collection<HudElement>
        get() = elementMap.values

    val elementMap = mutableMapOf<String, HudElement>()

    // 구현에서 추가해야 하는 정리 동작들

    open fun onEnable() {}
    open fun onDisable() {}

    final override fun enable() {
        onEnable()
    }

    final override fun disable() {
        onDisable()
        backendModules.close()
    }

    override fun draw(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        elementMap.forEach { (name, element) ->
            try {
                element.draw(guiGraphics, deltaTracker)
            } catch (exception: Exception) {
                debugLog("Error while drawing $name: $exception")
            }
        }
    }

    protected fun <T: HudElement> registerElement(name: String, element: T): T {
        elementMap[name] = element
        return element
    }

    class BackendModules : AutoCloseable {
        private val modules: MutableList<AutoCloseable> = mutableListOf()

        fun <T: AutoCloseable> register(module: T): T {
            modules.add(module)
            return module
        }

        override fun close() {
            modules.forEach { it.close() }
        }
    }
}
