package io.github.oni0nfr1.dynamicrider.client

import io.github.oni0nfr1.dynamicrider.client.command.Commands
import io.github.oni0nfr1.dynamicrider.client.command.DebugVariables
import io.github.oni0nfr1.dynamicrider.client.command.debug.DebugVarRegistry
import io.github.oni0nfr1.dynamicrider.client.command.debug.registerDbgVariables
import io.github.oni0nfr1.dynamicrider.client.config.DynRiderConfig
import io.github.oni0nfr1.dynamicrider.client.config.DynRiderKeybinds
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderRaceEndCallback
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderRaceEndCallback.RaceEndReason
import io.github.oni0nfr1.dynamicrider.client.event.scoreboard.RiderRaceStartCallback
import io.github.oni0nfr1.dynamicrider.client.event.util.HandleResult
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.impl.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.SpectateScene
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.mountSceneByEngine
import io.github.oni0nfr1.dynamicrider.client.rider.RaceSession
import io.github.oni0nfr1.dynamicrider.client.rider.mount.KartMountDetector
import io.github.oni0nfr1.dynamicrider.client.rider.mount.MountType
import io.github.oni0nfr1.dynamicrider.client.util.DynRiderJvmFlags
import io.github.oni0nfr1.dynamicrider.client.util.debugLog
import io.github.oni0nfr1.dynamicrider.client.util.infoLog
import io.github.oni0nfr1.dynamicrider.client.util.schedule.Ticker
import io.github.oni0nfr1.korigadier.api.korigadier
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.chat.Component

class DynamicRiderClient : ClientModInitializer {

    companion object {
        private var _instance: DynamicRiderClient? = null

        @JvmStatic
        var instance: DynamicRiderClient
            get() = _instance ?: error("DynamicRider Mod not initialized!")
            private set(value) {
                if (_instance != null) throw IllegalStateException("DynamicRider Mod has already been initialized!")
                _instance = value
            }
    }

    val stateManager = HudStateManager()

    var raceSession: RaceSession? = null
    var currentScene: HudScene? = null
        set(value) {
            field?.disable()
            field = value
            field?.enable()
        }

    val mountDetector = KartMountDetector(stateManager)

    override fun onInitializeClient() {
        // Bootstrap
        instance = this
        Ticker.init()

        // Load Config File
        DynRiderConfig.load()
        DynRiderConfig.apply(DynRiderConfig.currentData)
        DynRiderKeybinds.init()

        // register default events
        registerEvents()
        HudLayerRegistrationCallback.EVENT.register(this::registerHud)

        // register commands
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            korigadier(dispatcher) {
                include(Commands.setEngineCommand)

                if (DynRiderJvmFlags.devMode) {
                    val registry = DebugVarRegistry().apply {
                        scan(DebugVariables)
                    }

                    registerDbgVariables(
                        registry,
                        feedback = { source, msg ->
                            val component = Component.literal(msg)
                            source.sendFeedback(component)
                        }
                    )
                }
            }
        }

        infoLog("Load Complete.")
    }

    fun registerEvents() {
        ClientPlayConnectionEvents.DISCONNECT.register(this::onClientDisconnect)
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTickEnd)

        RiderRaceStartCallback.EVENT.register(this::onRaceStart)
        RiderRaceEndCallback.EVENT.register(this::onRaceEnd)
    }

    fun registerHud(layeredDrawer: LayeredDrawerWrapper) {
        layeredDrawer.attachLayerBefore(
            IdentifiedLayer.CHAT,
            ResourceStore.hudId,
            this::drawHud
        )
    }

    fun drawHud(guiGraphics: GuiGraphics, deltaTracker: DeltaTracker) {
        if (!DynRiderConfig.hudVisible || !DynRiderConfig.isModEnabled) return
        currentScene?.draw(guiGraphics, deltaTracker)
    }

    fun onClientTickEnd(client: Minecraft) {
        stateManager.recomposeIfDirty(this) {
            Ticker.runTaskLater(1) { // 안정적인 엔진 인식을 위한 디바운싱
                val engine = mountDetector.currentEngine()
                debugLog("detected engine: $engine")
                currentScene = when (mountDetector.playerMountStatus()) {
                    MountType.NOT_MOUNTED -> null
                    MountType.MOUNTED     -> mountSceneByEngine(stateManager, engine)
                    MountType.SPECTATOR   -> SpectateScene(stateManager)
                }
            }
        }
    }

    ////////////////////////////////// Event Handlers //////////////////////////////////

    fun onClientDisconnect(listener: ClientPacketListener, client: Minecraft) {
        // 메인 스레드에서 호출
        client.execute { RiderRaceEndCallback.EVENT.invoker().handle(RaceEndReason.DISCONNECT) }
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
