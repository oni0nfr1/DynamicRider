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
import io.github.oni0nfr1.dynamicrider.client.hud.scene.HudScene
import io.github.oni0nfr1.dynamicrider.client.hud.scene.lifecycle.HudSceneLifecycle
import io.github.oni0nfr1.dynamicrider.client.hud.runtime.LiveHudSceneContextFactory
import io.github.oni0nfr1.dynamicrider.client.hud.scene.loader.HudSceneResourceRegistry
import io.github.oni0nfr1.dynamicrider.client.rider.backend.RiderBackendRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.atlas.AtlasRegistry
import io.github.oni0nfr1.dynamicrider.client.resource.element.ElementRegistry
import io.github.oni0nfr1.dynamicrider.client.util.DynRiderJvmFlags
import io.github.oni0nfr1.dynamicrider.client.util.chatLog
import io.github.oni0nfr1.dynamicrider.client.util.debugLog
import io.github.oni0nfr1.dynamicrider.client.util.infoLog
import io.github.oni0nfr1.dynamicrider.client.util.warnLog
import io.github.oni0nfr1.dynamicrider.client.util.schedule.Ticker
import io.github.oni0nfr1.korigadier.api.korigadier
import io.github.oni0nfr1.skid.client.api.events.KartMountEvents
import io.github.oni0nfr1.skid.client.api.events.KartTachometerEvents
import io.github.oni0nfr1.skid.client.api.kart.KartRef
import io.github.oni0nfr1.skid.client.api.kart.KartSaddle
import io.github.oni0nfr1.skid.client.api.kart.subject
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.ChatFormatting
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

    var raceActive: Boolean = false
        private set
    var currentScene: HudScene<*>? = null
        set(value) {
            debugLog(
                "Replacing current HUD scene: previous=${field?.diagnosticName ?: "none"}, " +
                    "next=${value?.diagnosticName ?: "none"}"
            )
            field?.disable()
            field = value
            field?.enable()
            debugLog("Current HUD scene ready: scene=${field?.diagnosticName ?: "none"}")
        }

    override fun onInitializeClient() {
        // Bootstrap
        instance = this
        Ticker.init()
        AtlasRegistry.init()
        ElementRegistry.init()
        HudSceneResourceRegistry.init()

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
        KartMountEvents.SPECTATE_END.register(this::onKartSpectateEnd)

        KartTachometerEvents.RECEIVE.register(this::onTachometerMatch)

        RiderBackendRegistry.init()
    }

    fun onTachometerMatch(kart: KartRef, text: Component): KartTachometerEvents.Result
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

    fun onKartMount(kart: KartRef, rider: Player) {
        val client = Minecraft.getInstance()
        debugLog("Kart mount event received: entity=${kart.saddleId}")
        if (client.player?.subject != rider) {
            debugLog("Ignoring kart mount event for another rider: entity=${kart.saddleId}")
            return
        }

        val context = LiveHudSceneContextFactory.create(kart)
        if (context == null) {
            warnLog("Could not create ride HUD scene context: entity=${kart.saddleId}")
            currentScene = null
            return
        }
        debugLog("Creating ride HUD scene: state=${context.kartStateType.id}, entity=${kart.saddleId}")
        currentScene = HudSceneLifecycle.createRideScene(context)
        warnIfHudHidden()
    }

    fun onKartDismount(kartEntity: KartSaddle, rider: Player) {
        val client = Minecraft.getInstance()
        if (client.player?.subject != rider) return

        currentScene = null
    }

    fun onKartSpectate(kart: KartRef, spectator: Player, rider: Player) {
        val client = Minecraft.getInstance()
        if (client.player != spectator || client.player?.subject != rider) return

        currentScene = LiveHudSceneContextFactory.create(kart)
            ?.let(HudSceneLifecycle::createSpectateScene)
        warnIfHudHidden()
    }

    fun onKartSpectateEnd(kartEntity: KartSaddle, spectator: Player, rider: Player) {
        val client = Minecraft.getInstance()
        if (client.player != spectator) return

        currentScene = null
    }

    private fun warnIfHudHidden() {
        if (currentScene == null || !DynRiderConfig.isModEnabled || DynRiderConfig.hudVisible) return
        val toggleKey = DynRiderKeybinds.toggleHudKeyName
        val message = if (toggleKey == null) {
            Component.translatable("dynrider.hud.hidden_warning.unbound")
        } else {
            Component.translatable("dynrider.hud.hidden_warning.bound", toggleKey)
        }
        chatLog(
            message.withStyle(ChatFormatting.YELLOW)
        )
    }

    ////////////////////////////////// Event Handlers //////////////////////////////////

    fun onClientDisconnect(packetListener: ClientPacketListener, client: Minecraft) {
        // 메인 스레드에서 호출
        client.execute { RiderRaceEndCallback.EVENT.invoker().handle(RaceEndReason.DISCONNECT) }
    }

    fun onRaceStart(): HandleResult {
        raceActive = true
        return HandleResult.PASS
    }

    fun onRaceEnd(reason: RaceEndReason): HandleResult {
        raceActive = false
        return HandleResult.PASS
    }
}
