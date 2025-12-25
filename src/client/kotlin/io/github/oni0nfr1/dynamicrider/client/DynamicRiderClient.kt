package io.github.oni0nfr1.dynamicrider.client

import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.HudScene
import io.github.oni0nfr1.dynamicrider.client.rider.KartDetector
import io.github.oni0nfr1.dynamicrider.client.rider.KartNitroCounter
import io.github.oni0nfr1.dynamicrider.client.rider.KartSpeedMeasure
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation

class DynamicRiderClient : ClientModInitializer {

    companion object {
        private var _instance: DynamicRiderClient? = null
        val instance: DynamicRiderClient
            get() = _instance ?: error("DynamicRider Mod not initialized!")
    }

    val stateManager = HudStateManager()
    val hudId: ResourceLocation
        = ResourceLocation.fromNamespaceAndPath(ResourceStore.MOD_ID, "hud")

    var currentScene: HudScene? = null

    override fun onInitializeClient() {
        _instance = this
        initializeDetectors()

        HudLayerRegistrationCallback.EVENT.register { drawerWrapper ->
            drawerWrapper.attachLayerBefore(
                IdentifiedLayer.CHAT,
                hudId,
                this::drawHud
            )
        }
    }

    fun drawHud(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        currentScene?.draw(guiGraphics, deltaTracker)
    }

    fun initializeDetectors() {
        KartDetector.init()
        KartSpeedMeasure.init(stateManager)
        KartNitroCounter.init(stateManager)
    }
}
