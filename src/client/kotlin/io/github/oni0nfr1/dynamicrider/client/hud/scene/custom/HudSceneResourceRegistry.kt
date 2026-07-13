package io.github.oni0nfr1.dynamicrider.client.hud.scene.custom

import io.github.oni0nfr1.dynamicrider.client.ResourceStore
import kotlinx.serialization.SerializationException
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import java.util.concurrent.atomic.AtomicBoolean

object HudSceneResourceRegistry : SimpleSynchronousResourceReloadListener {
    private const val SCENE_DIRECTORY = "hud"
    private const val JSON_EXTENSION = ".json"

    private val reloadListenerId = ResourceLocation.fromNamespaceAndPath(
        ResourceStore.MOD_ID,
        "hud_scene_registry",
    )
    private val initialized = AtomicBoolean(false)

    @Volatile
    private var loadedScenes: Map<ResourceLocation, HudSceneSpec> = emptyMap()

    @Volatile
    private var loadErrors: Map<ResourceLocation, SerializationException> = emptyMap()

    fun init() {
        if (!initialized.compareAndSet(false, true)) return
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(this)
    }

    fun get(id: ResourceLocation): HudSceneSpec? = loadedScenes[id]

    fun getLoadError(id: ResourceLocation): SerializationException? = loadErrors[id]

    override fun getFabricId(): ResourceLocation = reloadListenerId

    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        val scenes = HashMap<ResourceLocation, HudSceneSpec>()
        val errors = HashMap<ResourceLocation, SerializationException>()
        val resources = resourceManager.listResources(SCENE_DIRECTORY) { location ->
            location.path.endsWith(JSON_EXTENSION)
        }

        for ((resourceLocation, resource) in resources) {
            val sceneId = sceneIdOf(resourceLocation)
            try {
                val spec = resource.openAsReader().use { HudSceneCodec.decode(it.readText()) }
                scenes[sceneId] = spec
            } catch (exception: Exception) {
                val serializationException = exception as? SerializationException
                    ?: SerializationException("Failed to read HUD scene $resourceLocation", exception)
                errors[sceneId] = serializationException
                ResourceStore.logger.error(
                    "[DynamicRider] Failed to load HUD scene '{}'",
                    resourceLocation,
                    exception,
                )
            }
        }

        loadedScenes = scenes.toMap()
        loadErrors = errors.toMap()
        ResourceStore.logger.info("[DynamicRider] Loaded {} HUD scene(s)", loadedScenes.size)
    }

    private fun sceneIdOf(resourceId: ResourceLocation): ResourceLocation {
        val path = resourceId.path
            .removePrefix("$SCENE_DIRECTORY/")
            .removeSuffix(JSON_EXTENSION)
        return ResourceLocation.fromNamespaceAndPath(resourceId.namespace, path)
    }
}
