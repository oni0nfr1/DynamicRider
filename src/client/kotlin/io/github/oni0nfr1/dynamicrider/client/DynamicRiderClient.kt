package io.github.oni0nfr1.dynamicrider.client

import io.github.oni0nfr1.dynamicrider.client.hud.scenes.ExampleScene
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.HudScene
import io.github.oni0nfr1.dynamicrider.client.rider.mount.KartMountDetector
import io.github.oni0nfr1.dynamicrider.client.rider.mount.MountType
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.RaceClock
import io.github.oni0nfr1.dynamicrider.client.rider.sidebar.RankingManager
import io.github.oni0nfr1.dynamicrider.client.util.schedule.Ticker
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
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
        set(value) {
            field?.disable()
            field = value
            field?.enable()
        }

    val mountDetector = KartMountDetector(stateManager)

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
        stateManager.recomposeIfDirty(this) {
            ResourceStore.logger.info("successfully initialized dynrider")
            mountDetector.playerMountStatus() // 의존관계 등록용. 읽은 값은 버림
        }
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            stateManager.recomposeIfDirty(this) {
                currentScene = when (mountDetector.playerMountStatus()) {
                    MountType.NOT_MOUNTED -> null
                    MountType.MOUNTED     -> ExampleScene(stateManager)
                    MountType.SPECTATOR   -> ExampleScene(stateManager)
                }
            }
        }
    }

    fun drawHud(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        currentScene?.draw(guiGraphics, deltaTracker)
    }

    fun initializeDetectors() {
        RankingManager.init(stateManager)
        RaceClock.init(stateManager)
    }
}
