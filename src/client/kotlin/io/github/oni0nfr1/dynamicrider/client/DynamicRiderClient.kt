package io.github.oni0nfr1.dynamicrider.client

import io.github.oni0nfr1.dynamicrider.client.hud.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.HudScene_
import io.github.oni0nfr1.dynamicrider.client.rider.KartDetector
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartGaugeMeasure
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartNitroCounter
import io.github.oni0nfr1.dynamicrider.client.rider.actionbar.KartSpeedMeasure
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.RaceClock
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.RankingManager
import io.github.oni0nfr1.dynamicrider.client.util.schedule.Ticker
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
        val scene: HudScene_?
            get() = instance.currentScene_
    }

    val stateManager = HudStateManager()
    val hudId: ResourceLocation
        = ResourceLocation.fromNamespaceAndPath(ResourceStore.MOD_ID, "hud")

    // TODO
    var currentScene_: HudScene_? = null

    // TODO: 나중에 지우기
    var currentScene: HudScene? = null
        set(value) {
            field?.disable()
            field = value
            field?.enable()
        }

    override fun onInitializeClient() {
        _instance = this
        Ticker.init()
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
        KartDetector.init(stateManager)
        KartSpeedMeasure.init(stateManager)
        KartNitroCounter.init(stateManager)
        KartGaugeMeasure.init(stateManager)

        RankingManager.init(stateManager)
        RaceClock.init(stateManager)
    }
}
