package io.github.oni0nfr1.dynamicrider.client.command

import com.mojang.brigadier.Command
import io.github.oni0nfr1.dynamicrider.client.DynamicRiderClient
import io.github.oni0nfr1.dynamicrider.client.config.DynRiderConfig
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.CustomHudSceneLoader
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HudSceneMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HudSceneResolver
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.HudUiMode
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.LoadCustomHudResult
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.configKey
import io.github.oni0nfr1.dynamicrider.client.hud.scene.config.parseEngineType
import io.github.oni0nfr1.korigadier.api.Args
import io.github.oni0nfr1.korigadier.api.fragment
import io.github.oni0nfr1.korigadier.api.get
import io.github.oni0nfr1.skid.client.api.engine.KartEngine
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.Component

object Commands {

    private val engines = KartEngine.Type.entries
        .filter { !it.isDummy }
        .map { it.engineName to it.engineCode }

    private val dummyEngines = KartEngine.Type.entries
        .filter { it.isDummy }
        .map { it.engineName to it.engineCode }

    val setEngineCommand = fragment<FabricClientCommandSource> {
        literal("engine") {
            argument("engine-number", Args.int()) {
                executes { ctx ->
                    val engineNumber = ctx.get<Int>("engine-number")
                    sendCommand("trigger setengine set $engineNumber")
                    Command.SINGLE_SUCCESS
                }
            }

            engines.forEach { engine ->
                literal(engine.first) {
                    executes {
                        sendCommand("trigger setengine set ${engine.second}")
                        Command.SINGLE_SUCCESS
                    }
                }
            }
        }

        literal("dengine") {
            argument("engine-number", Args.int()) {
                executes { ctx ->
                    val engineNumber = ctx.get<Int>("engine-number")
                    sendCommand("trigger setdummyengine set $engineNumber")
                    Command.SINGLE_SUCCESS
                }
            }

            dummyEngines.forEach { engine ->
                literal(engine.first) {
                    executes {
                        sendCommand("trigger setdummyengine set ${engine.second}")
                        Command.SINGLE_SUCCESS
                    }
                }
            }
        }
    }

    val uiCommand = fragment<FabricClientCommandSource> {
        literal("dynrider") {
            literal("set-ui") {
                literal("default") {
                    executes { ctx ->
                        DynRiderConfig.setUiMode(HudUiMode.DEFAULT)
                        val refreshed = DynamicRiderClient.instance.refreshCurrentScene()
                        ctx.source.feedback("UI mode: default" + if (refreshed) " (scene refreshed)" else "")
                        Command.SINGLE_SUCCESS
                    }
                }

                literal("custom") {
                    executes { ctx ->
                        DynRiderConfig.setUiMode(HudUiMode.CUSTOM)

                        val key = DynamicRiderClient.instance.currentSceneKey
                        if (key != null && DynRiderConfig.customSceneName(key.engineKey, key.mode) == null) {
                            when (val result = CustomHudSceneLoader.ensureDefaultFor(key)) {
                                is LoadCustomHudResult.Success -> {
                                    DynRiderConfig.setCustomSceneName(key.engineKey, key.mode, result.scene.fileName)
                                    ctx.source.feedback("Created custom UI mapping: ${key.engineKey}/${key.mode.name.lowercase()} -> ${result.scene.fileName}")
                                }

                                is LoadCustomHudResult.Failure -> {
                                    ctx.source.feedback("Custom UI mode enabled, but default custom scene failed: ${result.reason}")
                                }
                            }
                        }

                        val refreshed = DynamicRiderClient.instance.refreshCurrentScene()
                        ctx.source.feedback("UI mode: custom" + if (refreshed) " (scene refreshed)" else "")
                        Command.SINGLE_SUCCESS
                    }
                }
            }

            literal("load") {
                argument("name", Args.word()) {
                    executes { ctx ->
                        val name = ctx.get<String>("name")
                        when (val result = CustomHudSceneLoader.load(name)) {
                            is LoadCustomHudResult.Success -> {
                                val scene = result.scene
                                DynRiderConfig.setCustomSceneName(scene.key.engineKey, scene.key.mode, scene.fileName)
                                val refreshed = DynamicRiderClient.instance.currentSceneKey == scene.key &&
                                        DynamicRiderClient.instance.refreshCurrentScene()

                                ctx.source.feedback(
                                    "Loaded custom UI '${scene.displayName}' for ${scene.key.engineKey}/${scene.key.mode.name.lowercase()}" +
                                            if (refreshed) " (scene refreshed)" else ""
                                )
                                Command.SINGLE_SUCCESS
                            }

                            is LoadCustomHudResult.Failure -> {
                                ctx.source.feedback("Failed to load custom UI '${result.fileName}': ${result.reason}")
                                0
                            }
                        }
                    }
                }
            }

            literal("ui-name") {
                executes { ctx ->
                    val key = DynamicRiderClient.instance.currentSceneKey
                    if (key == null) {
                        ctx.source.feedback("Current Scene Name: none")
                    } else {
                        ctx.source.feedback("Current Scene Name: ${HudSceneResolver.sceneName(key.engine, key.mode)}")
                    }
                    Command.SINGLE_SUCCESS
                }

                argument("engine", Args.word()) {
                    executes { ctx ->
                        val engineName = ctx.get<String>("engine")
                        val engine = parseEngineType(engineName)
                        if (engine == null) {
                            ctx.source.feedback("Unknown engine: $engineName")
                            return@executes 0
                        }

                        ctx.source.feedback("Name of Scene for ${engine.configKey}:")
                        ctx.source.feedback("ride: ${HudSceneResolver.sceneName(engine, HudSceneMode.RIDE)}")
                        ctx.source.feedback("spectate: ${HudSceneResolver.sceneName(engine, HudSceneMode.SPECTATE)}")
                        Command.SINGLE_SUCCESS
                    }
                }
            }

            literal("list-ui") {
                executes { ctx ->
                    val entries = CustomHudSceneLoader.list()
                    if (entries.isEmpty()) {
                        ctx.source.feedback("Available custom UIs: none")
                        return@executes Command.SINGLE_SUCCESS
                    }

                    ctx.source.feedback("Available custom UIs:")
                    entries.forEach { entry ->
                        if (entry.error != null) {
                            ctx.source.feedback("- ${entry.fileName}: invalid (${entry.error})")
                        } else {
                            ctx.source.feedback("- ${entry.fileName}: ${entry.displayName}, engine=${entry.engine}, mode=${entry.mode?.name?.lowercase()}")
                        }
                    }
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }

    private fun FabricClientCommandSource.feedback(message: String) {
        sendFeedback(Component.literal(message))
    }

}
