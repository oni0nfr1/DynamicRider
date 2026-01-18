package io.github.oni0nfr1.dynamicrider.client

import io.github.oni0nfr1.dynamicrider.client.config.DynRiderConfig
import io.github.oni0nfr1.dynamicrider.client.config.DynRiderKeybinds
import io.github.oni0nfr1.dynamicrider.client.event.RiderRaceEndCallback
import io.github.oni0nfr1.dynamicrider.client.event.RiderRaceEndCallback.RaceEndReason
import io.github.oni0nfr1.dynamicrider.client.event.RiderRaceStartCallback
import io.github.oni0nfr1.dynamicrider.client.event.RiderLapFinishCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.ExampleScene
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.HudScene
import io.github.oni0nfr1.dynamicrider.client.rider.RaceSession
import io.github.oni0nfr1.dynamicrider.client.rider.chat.LapMessage
import io.github.oni0nfr1.dynamicrider.client.rider.mount.KartMountDetector
import io.github.oni0nfr1.dynamicrider.client.rider.mount.MountType
import io.github.oni0nfr1.dynamicrider.client.util.debugInfo
import io.github.oni0nfr1.dynamicrider.client.util.schedule.Ticker
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

class DynamicRiderClient : ClientModInitializer {

    companion object {
        private var _instance: DynamicRiderClient? = null
        var instance: DynamicRiderClient
            get() = _instance ?: error("DynamicRider Mod not initialized!")
            private set(value) { _instance = value }
    }

    val stateManager = HudStateManager()
    val hudId: ResourceLocation
        = ResourceLocation.fromNamespaceAndPath(ResourceStore.MOD_ID, "hud")

    var raceSession: RaceSession? = null
    var currentScene: HudScene? = null
        set(value) {
            field?.disable()
            field = value
            field?.enable()
        }

    val mountDetector = KartMountDetector(stateManager)

    override fun onInitializeClient() {
        instance = this
        Ticker.init()
        DynRiderKeybinds.init()
        registerEvents()

        HudLayerRegistrationCallback.EVENT.register { drawerWrapper ->
            drawerWrapper.attachLayerBefore(
                IdentifiedLayer.CHAT,
                hudId,
                this::drawHud
            )
        }

        ResourceStore.logger.info("[DynamicRider] Load Complete.")
    }

    fun registerEvents() {
        ClientPlayConnectionEvents.DISCONNECT.register(this::onClientDisconnect)
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTickEnd)
        ClientReceiveMessageEvents.GAME.register { component, _ ->
            dispatchLapMessage(component)
        }
        ClientReceiveMessageEvents.CHAT.register { component, _, _, _, _ ->
            dispatchLapMessage(component)
        }

        RiderRaceStartCallback.EVENT.register(this::onRaceStart)
        RiderRaceEndCallback.EVENT.register(this::onRaceEnd)
    }

    fun drawHud(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        if (!DynRiderConfig.hudVisible) return
        currentScene?.draw(guiGraphics, deltaTracker)
    }

    fun onClientTickEnd(client: Minecraft) {
        stateManager.recomposeIfDirty(this) {
            currentScene = when (mountDetector.playerMountStatus()) {
                MountType.NOT_MOUNTED -> null
                MountType.MOUNTED     -> ExampleScene(stateManager)
                MountType.SPECTATOR   -> ExampleScene(stateManager)
            }
        }
    }

    ////////////////////////////////// Event Handlers //////////////////////////////////

    fun onClientDisconnect(listener: ClientPacketListener, client: Minecraft) {
        ResourceStore.logger.debugInfo("[DynamicRider] Race ended due to client disconnection")
        RiderRaceEndCallback.EVENT.invoker().handle(RaceEndReason.DISCONNECT)
    }

    fun onGameMessage(component: Component, isActionbar: Boolean) {

    }

    private fun dispatchLapMessage(component: Component) {
        val scoreboard = Minecraft.getInstance().level?.scoreboard ?: return
        val msg = LapMessage.parseLapMessage(component, scoreboard) ?: return
        RiderLapFinishCallback.EVENT.invoker().handle(msg)
    }

    fun onRaceStart(): HandleResult {
        raceSession = RaceSession(stateManager)
        return HandleResult.PASS
    }

    fun onRaceEnd(reason: RaceEndReason): HandleResult {
        raceSession?.close()
        raceSession = null
        return HandleResult.PASS
    }
}
