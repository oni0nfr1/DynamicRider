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
import io.github.oni0nfr1.dynamicrider.client.hud.state.HudStateManager
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.v2.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.makeScene
import io.github.oni0nfr1.dynamicrider.client.hud.scenes.makeSpectateScene
import io.github.oni0nfr1.dynamicrider.client.rider.RaceSession
import io.github.oni0nfr1.dynamicrider.client.rider.mount.KartMountDetector
import io.github.oni0nfr1.dynamicrider.client.rider.v2.RiderBackendRegistry
import io.github.oni0nfr1.dynamicrider.client.util.DynRiderJvmFlags
import io.github.oni0nfr1.dynamicrider.client.util.debugLog
import io.github.oni0nfr1.dynamicrider.client.util.infoLog
import io.github.oni0nfr1.dynamicrider.client.util.schedule.Ticker
import io.github.oni0nfr1.korigadier.api.korigadier
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import io.github.oni0nfr1.skid.client.api.events.KartMountEvents
import io.github.oni0nfr1.skid.client.api.events.KartTachometerEvents
import io.github.oni0nfr1.skid.client.api.kart.Kart
import io.github.oni0nfr1.skid.client.api.kart.KartEntity
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

        RiderRaceStartCallback.EVENT.register(this::onRaceStart)
        RiderRaceEndCallback.EVENT.register(this::onRaceEnd)

        KartMountEvents.MOUNT.register(this::onKartMount)
        KartMountEvents.DISMOUNT.register(this::onKartDismount)
        KartMountEvents.SPECTATE.register(this::onKartSpectate)

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

    fun onKartMount(kartEntity: KartEntity, rider: Player) {
        val client = Minecraft.getInstance()
        if (client.player?.subject != rider) return

        val engineType = client.kartEngineType

        debugLog("detected engine: ${engineType?.engineName}")
        currentScene = if (engineType != null) makeScene(engineType) else null
    }

    fun onKartDismount(kartEntity: KartEntity, rider: Player) {
        val client = Minecraft.getInstance()
        if (client.player?.subject != rider) return

        currentScene = null
    }

    fun onKartSpectate(kartEntity: KartEntity, spectator: Player, rider: Player) {
        val client = Minecraft.getInstance()
        if (client.player != spectator || client.player?.subject != rider) return

        val engineType = client.kartEngineType

        debugLog("detected engine: ${engineType?.engineName}")
        currentScene = if (engineType != null) makeSpectateScene(engineType) else null
    }

    ////////////////////////////////// Event Handlers //////////////////////////////////

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
