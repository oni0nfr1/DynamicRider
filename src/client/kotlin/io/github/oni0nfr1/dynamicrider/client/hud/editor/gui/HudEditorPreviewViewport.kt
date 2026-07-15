package io.github.oni0nfr1.dynamicrider.client.hud.editor.gui

import io.github.oni0nfr1.dynamicrider.client.hud.ElementHolder

/** 화면 resize에도 같은 preview scene이 최신 캔버스 크기를 읽게 하는 가변 viewport다. */
class HudEditorPreviewViewport : ElementHolder {
    override var width: Int = 1
        private set
    override var height: Int = 1
        private set

    fun resize(width: Int, height: Int) {
        this.width = width.coerceAtLeast(1)
        this.height = height.coerceAtLeast(1)
    }
}
