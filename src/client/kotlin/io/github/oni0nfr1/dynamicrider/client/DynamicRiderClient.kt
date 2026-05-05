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
import io.github.oni0nfr1.dynamicrider.client.hud.VanillaSuppression
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HudSceneKey
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HudSceneResolver
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.scene.layouts.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.makeScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.makeSpectateScene
import io.github.oni0nfr1.dynamicrider.client.rider.legacy.RaceSession
import io.github.oni0nfr1.dynamicrider.client.rider.backend.RiderBackendRegistry
import io.github.oni0nfr1.dynamicrider.client.util.DynRiderJvmFlags
import io.github.oni0nfr1.dynamicrider.client.util.debugLog
import io.github.oni0nfr1.dynamicrider.client.util.infoLog
import io.github.oni0nfr1.dynamicrider.client.util.schedule.Ticker
import io.github.oni0nfr1.korigadier.api.korigadier
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.engine.NitroEngine
import io.github.oni0nfr1.skid.client.api.events.KartMountEvents
import io.github.oni0nfr1.skid.client.api.events.KartTachometerEvents
import io.github.oni0nfr1.skid.client.api.kart.Kart
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import io.github.oni0nfr1.skid.client.api.kart.KartSaddleEntity
import io.github.oni0nfr1.skid.client.api.kart.kart
import io.github.oni0nfr1.skid.client.api.kart.kartEngineType
import io.github.oni0nfr1.skid.client.api.kart.subject
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player

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
    var currentSceneKey: HudSceneKey? = null
        private set
    private var currentSceneKart: KartRef.Specific<NitroEngine>? = null
    var currentScene: HudScene<*>? = null
        set(value) {
            field?.disable()
            field = value
            field?.enable()
        }

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
                include(Commands.uiCommand)

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

        RiderRaceStartCallback.EVENT.register(this::onRaceStart)
        RiderRaceEndCallback.EVENT.register(this::onRaceEnd)

        KartMountEvents.MOUNT.register(this::onKartMount)
        KartMountEvents.DISMOUNT.register(this::onKartDismount)
        KartMountEvents.SPECTATE.register(this::onKartSpectate)
        KartMountEvents.SPECTATE_END.register(this::onKartSpectateEnd)

        KartTachometerEvents.RECEIVE.register(this::onTachometerMatch)

        RiderBackendRegistry.init()
    }

    fun onTachometerMatch(kart: Kart, engine: KartEngine, text: Component): KartTachometerEvents.Result
    =   if (VanillaSuppression.suppressVanillaKartState) KartTachometerEvents.Result.BLOCK
        else KartTachometerEvents.Result.SHOW

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

    fun onKartMount(kartEntity: KartSaddleEntity, rider: Player) {
        val client = Minecraft.getInstance()
        if (client.player?.subject != rider) return

        val engineType = client.kartEngineType
        val sceneKart = kartEntity.kart?.access {
            (engine as? NitroEngine)?.let { KartRef.specify(it) }
        }

        debugLog("detected engine: ${engineType?.engineName}")
        currentSceneKey = engineType?.let { HudSceneKey(it, HudSceneMode.RIDE) }
        currentSceneKart = sceneKart
        currentScene = if (engineType != null && sceneKart != null) makeScene(engineType, sceneKart) else null
    }

    fun onKartDismount(kartEntity: KartSaddleEntity, rider: Player) {
        val client = Minecraft.getInstance()
        if (client.player?.subject != rider) return

        currentSceneKey = null
        currentSceneKart = null
        currentScene = null
    }

    fun onKartSpectate(kartEntity: KartSaddleEntity, spectator: Player, rider: Player) {
        val client = Minecraft.getInstance()
        if (client.player != spectator || client.player?.subject != rider) return

        val engineType = client.kartEngineType
        val sceneKart = kartEntity.kart?.access {
            (engine as? NitroEngine)?.let { KartRef.specify(it) }
        }

        debugLog("detected engine: ${engineType?.engineName}")
        currentSceneKey = engineType?.let { HudSceneKey(it, HudSceneMode.SPECTATE) }
        currentSceneKart = sceneKart
        currentScene = if (engineType != null && sceneKart != null) makeSpectateScene(engineType, sceneKart) else null
    }

    fun onKartSpectateEnd(kartEntity: KartSaddleEntity, spectator: Player, rider: Player) {
        val client = Minecraft.getInstance()
        if (client.player?.subject != rider) return

        currentSceneKey = null
        currentSceneKart = null
        currentScene = null
    }

    ////////////////////////////////// Event Handlers //////////////////////////////////

    fun refreshCurrentScene(): Boolean {
        val key = currentSceneKey ?: return false
        val kart = currentSceneKart ?: return false

        currentScene = HudSceneResolver.createScene(key.engine, key.mode, kart)
        return true
    }

    fun onClientDisconnect(packetListener: ClientPacketListener, client: Minecraft) {
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
