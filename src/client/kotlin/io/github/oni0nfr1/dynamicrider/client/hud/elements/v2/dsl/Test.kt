package io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.dsl

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import io.github.oni0nfr1.dynamicrider.client.config.DynRiderConfig
import io.github.oni0nfr1.dynamicrider.client.hud.HudAnchor
import io.github.oni0nfr1.dynamicrider.client.hud.elements.v2.impl.HudElementImpl
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import org.joml.Vector2i

object SomeObject {
    val valueToRead: Int = 0
}

class SomeElementBuilder : ElementDataBuilder<SomeElementBuilder, SomeElement>() {
    var a = 0
    lateinit var valueToRead: () -> Int
}


class SomeElement(val builder: SomeElementBuilder) : HudElementImpl<SomeElementBuilder, SomeElement>(builder) {
    val a = builder.a
    val valueToRead: Int
        get() = builder.valueToRead()

    override fun resolveSize() {
        TODO("Not yet implemented")
    }

    override fun render(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        TODO("Not yet implemented")
    }
}

fun foo() {
    HudApiTest.element = element<SomeElementBuilder, SomeElement> {
        layout {
            screenAnchor = HudAnchor.BOTTOM_CENTER
            elementAnchor = HudAnchor.BOTTOM_CENTER
            position = Vector2i(0, -10)
        }
        a = 10
        valueToRead = SomeObject::valueToRead
    }

    HudLayerRegistrationCallback.EVENT.register(HudApiTest::registerHud)
}

object HudApiTest {
    var element: SomeElement? = null

    fun registerHud(layeredDrawer: LayeredDrawerWrapper) {
        layeredDrawer.attachLayerBefore(
            IdentifiedLayer.CHAT,
            ResourceStore.hudId,
            this::drawHud,
        )
    }

    fun drawHud(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        if (!DynRiderConfig.hudVisible || !DynRiderConfig.isModEnabled) return
        element?.draw(guiGraphics, deltaTracker)
    }
}

